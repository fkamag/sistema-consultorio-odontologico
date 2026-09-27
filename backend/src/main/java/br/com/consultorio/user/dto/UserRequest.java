package br.com.consultorio.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UserRequest(

        @NotBlank(message = "Nome é obrigatório")
        @Size(max = 150, message = "Nome deve ter no máximo 150 caracteres")
        String name,

        @NotBlank(message = "E-mail é obrigatório")
        @Email(message = "E-mail inválido")
        @Size(max = 150)
        String email,

        // Nulo em atualização sem troca de senha
        @Size(min = 8, message = "Senha deve ter no mínimo 8 caracteres")
        @Pattern(
                regexp = "^(?=.*[A-Z])(?=.*[0-9])(?=.*[^A-Za-z0-9]).{8,}$",
                message = "Senha deve conter letra maiúscula, número e caractere especial"
        )
        String password,

        @NotBlank(message = "Perfil é obrigatório")
        @Pattern(regexp = "ADMIN|SECRETARIA|DENTISTA", message = "Perfil inválido")
        String role
) {}
