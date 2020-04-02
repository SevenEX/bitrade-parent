package cn.ztuo.bitrade.entity;

import cn.ztuo.bitrade.annotation.NoDuplicateEmail;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;

import javax.validation.constraints.Pattern;

/**
 * @author Seven
 * @date 2019年12月29日
 */
@Data
public class LoginByEmail {

    @NotBlank(message = "{LoginByEmail.email.null}")
    @Email(message = "{LoginByEmail.email.format}")
    @NoDuplicateEmail
    private String email;

    @Pattern(regexp="^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)[\\s\\S]{8,16}$",message = "{LoginByEmail.password.Pattern}")
    private String password;

    @NotBlank(message =  "{LoginByEmail.country.null}")
    private String country;

//    @NotBlank
    private String ticket;

//    @NotBlank
    private String randStr;

    private String promotion;

}
