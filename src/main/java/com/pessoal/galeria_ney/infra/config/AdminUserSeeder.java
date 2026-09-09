package com.pessoal.galeria_ney.infra.config;

import com.pessoal.galeria_ney.domain.UserRole;
import com.pessoal.galeria_ney.domain.Usuario;
import com.pessoal.galeria_ney.repository.UsuarioRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class AdminUserSeeder implements CommandLineRunner {
    private final UsuarioRepository repository;

    public AdminUserSeeder(UsuarioRepository repository) {
        this.repository = repository;
    }

    @Override
    public void run(String... args) throws Exception {
        String loginAdmin = "ney@galeria.com";

        if (repository.findByLogin(loginAdmin) == null) {

            String senhaCriptografada = new BCryptPasswordEncoder().encode("senha123");

            Usuario admin = new Usuario();
            admin.setLogin(loginAdmin);
            admin.setSenha(senhaCriptografada);
            admin.setRole(UserRole.ADMIN);
            admin.setDescricao("Criador e Administrador Principal da Plataforma.");

            repository.save(admin);

            System.out.println("✅ Seeder executado: Usuário ADMIN criado com sucesso!");
        } else {
            System.out.println("⚡ Seeder: Usuário ADMIN já existe no banco de dados.");
        }
    }
}