package org.gepron1x.mixxed.form;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class ProfileEditForm {
    private String bio;
    private MultipartFile avatarFile;
}
