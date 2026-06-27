package com.pessoal.galeria_ney.service.storage;

import org.springframework.web.multipart.MultipartFile;

public interface MidiaStorageService {
    String upload(MultipartFile arquivo);
}