package com.pessoal.galeria_ney.controller;

import com.pessoal.galeria_ney.domain.UserRole;
import com.pessoal.galeria_ney.domain.Usuario;
import com.pessoal.galeria_ney.dto.AuthenticationDTO;
import com.pessoal.galeria_ney.dto.LoginResponseDTO;
import com.pessoal.galeria_ney.dto.RegisterDTO;
import com.pessoal.galeria_ney.repository.UsuarioRepository;
import com.pessoal.galeria_ney.service.TokenService;
import com.pessoal.galeria_ney.service.storage.MidiaStorageService;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("auth")
public class AuthenticationController {

    private final AuthenticationManager authenticationManager;
    private final UsuarioRepository repository;
    private final TokenService tokenService;
    private final MidiaStorageService storageService;

    public AuthenticationController(AuthenticationManager authenticationManager, UsuarioRepository repository, TokenService tokenService, MidiaStorageService storageService) {
        this.authenticationManager = authenticationManager;
        this.repository = repository;
        this.tokenService = tokenService;
        this.storageService = storageService;
    }


    @PostMapping("/login")
    public ResponseEntity login(@RequestBody @Valid AuthenticationDTO data) {
        var usernamePassword = new UsernamePasswordAuthenticationToken(data.login(), data.senha());
        var auth = this.authenticationManager.authenticate(usernamePassword);

        var token = tokenService.generateToken((Usuario) auth.getPrincipal());

        return ResponseEntity.ok(new LoginResponseDTO(token));
    }

    @PostMapping(value = "/register", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity register(
            @RequestParam("login") String login,
            @RequestParam("senha") String senha,
            @RequestParam("role") UserRole role,
            @RequestParam(value = "descricao", required = false) String descricao,
            @RequestPart(value = "foto", required = false) MultipartFile foto) {

        if (this.repository.findByLogin(login) != null) return ResponseEntity.badRequest().build();

        String encryptedPassword = new BCryptPasswordEncoder().encode(senha);
        Usuario newUser = new Usuario(login, encryptedPassword, role);
        newUser.setDescricao(descricao);

        if (foto != null && !foto.isEmpty()) {
            String fotoUrl = storageService.upload(foto);
            newUser.setFotoPerfil(fotoUrl);
        }

        this.repository.save(newUser);

        return ResponseEntity.ok().build();
    }

    @PutMapping(value = "/usuarios/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity atualizarUsuario(
            @PathVariable java.util.UUID id,
            @RequestParam("login") String login,
            @RequestParam(value = "senha", required = false) String senha,
            @RequestParam("role") UserRole role,
            @RequestParam(value = "descricao", required = false) String descricao,
            @RequestPart(value = "foto", required = false) MultipartFile foto) {

        var usuarioOptional = this.repository.findById(id);
        if (usuarioOptional.isEmpty()) return ResponseEntity.notFound().build();

        Usuario usuario = usuarioOptional.get();
        usuario.setLogin(login);
        usuario.setRole(role);
        usuario.setDescricao(descricao);

        // Só atualiza a senha se o administrador digitou uma nova no front-end
        if (senha != null && !senha.trim().isEmpty()) {
            usuario.setSenha(new BCryptPasswordEncoder().encode(senha));
        }

        // Só faz upload se uma foto nova for enviada
        if (foto != null && !foto.isEmpty()) {
            String fotoUrl = storageService.upload(foto);
            usuario.setFotoPerfil(fotoUrl);
        }

        this.repository.save(usuario);

        return ResponseEntity.ok().build();
    }
}