package ru.kpfu.itis.services;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import ru.kpfu.itis.form.LoginForm;
import ru.kpfu.itis.form.UserForm;
import ru.kpfu.itis.models.Auth;
import ru.kpfu.itis.models.User;
import ru.kpfu.itis.repositories.AuthRepository;
import ru.kpfu.itis.repositories.UsersRepository;

import javax.servlet.http.Cookie;
import java.util.UUID;

public class UsersServiceImpl implements UsersService {

    private UsersRepository usersRepository;
    private AuthRepository authRepository;
    private PasswordEncoder passwordEncoder;

    public UsersServiceImpl(UsersRepository usersRepository) {
        this.usersRepository = usersRepository;
        this.passwordEncoder = new BCryptPasswordEncoder();
    }

    public UsersServiceImpl(UsersRepository usersRepository, AuthRepository authRepository) {
        this.usersRepository = usersRepository;
        this.authRepository = authRepository;
        this.passwordEncoder =  new BCryptPasswordEncoder();
    }

    @Override
    public User register(UserForm userForm) {
        User user = new User();
        user.setNickname(userForm.getNickname());
        user.setEmail(userForm.getEmail());

        String passwordHash = new BCryptPasswordEncoder().encode(userForm.getPassword());

        user.setPasswordHash(passwordHash);

        return usersRepository.save(user);
    }

    @Override
    public Cookie signIn(LoginForm loginForm) {

        User user = usersRepository.findByLogin(loginForm.getLogin());
        System.out.println(loginForm.getLogin());
        System.out.println(user);
        if (user != null) {
            if (passwordEncoder.matches(loginForm.getPassword(), user.getPasswordHash())) {
                System.out.println("Вход выполнен!");
                String cookieValue = UUID.randomUUID().toString();
                Auth auth = new Auth();
                auth.setUser(user);
                auth.setCookieValue(cookieValue);
                authRepository.save(auth);
                Cookie cookie = new Cookie("auth", cookieValue);
                cookie.setMaxAge(10 * 60 * 60);
                return cookie;
            } else {
                System.out.println("Вход не выполнен!");
            }
        }
        return null;
    }

    @Override
    public User findUserByCookieValue(String cookieValue) {
        Auth auth = authRepository.findByCookieValue(cookieValue);
        if (auth != null) {
            return auth.getUser();
        } else {
            return null;
        }
    }
}
