package ru.kpfu.itis.servlets;

import com.google.gson.Gson;
import com.vk.api.sdk.client.Lang;
import com.vk.api.sdk.client.TransportClient;
import com.vk.api.sdk.client.VkApiClient;
import com.vk.api.sdk.client.actors.UserActor;
import com.vk.api.sdk.exceptions.ApiException;
import com.vk.api.sdk.exceptions.ClientException;
import com.vk.api.sdk.httpclient.HttpTransportClient;
import com.vk.api.sdk.objects.UserAuthResponse;
import com.vk.api.sdk.objects.users.UserXtrCounters;
import ru.kpfu.itis.form.UserForm;
import ru.kpfu.itis.repositories.UsersRepository;
import ru.kpfu.itis.repositories.UsersRepositoryImpl;
import ru.kpfu.itis.services.UsersService;
import ru.kpfu.itis.services.UsersServiceImpl;

import javax.servlet.ServletException;
import javax.servlet.UnavailableException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

@WebServlet("/vk")
public class VkRegistrationServlet extends HttpServlet {

    private UsersService usersService;

    private final String URL = "jdbc:postgresql://localhost:5432/maven";
    private final String USERNAME = "postgres";
    private final String PASSWORD = "sadafa54ga";

    @Override
    public void init() throws ServletException {
        try {
            Class.forName("org.postgresql.Driver");
            Connection connection = DriverManager.getConnection(URL, USERNAME, PASSWORD);

            UsersRepository usersRepository = new UsersRepositoryImpl(connection);
            usersService = new UsersServiceImpl(usersRepository);
        } catch (SQLException | ClassNotFoundException e) {
            throw new UnavailableException("Сайт недоступен!!!");
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            TransportClient transportClient = HttpTransportClient.getInstance();
            VkApiClient vk = new VkApiClient(transportClient, new Gson(), 5);

            String code = req.getParameter("code");
            UserAuthResponse authResponse = vk.oauth()
                    .userAuthorizationCodeFlow(7998861, "i1PRtAQB2gUPzzwLuuT2", "http://localhost:8080/vk", code)
                    .execute();



            UserActor actor = new UserActor(authResponse.getUserId(), authResponse.getAccessToken());
            System.out.println(actor.getAccessToken());


            UserXtrCounters account = vk.users().get(actor)
                    .userIds(actor.getId().toString())
                    .lang(Lang.RU)
                    .unsafeParam("v", "5.131")
                    .execute().get(0);

            System.out.println(account.getLastName() + account.getFirstName());

           UserForm userForm = new UserForm();
           userForm.setNickname(account.getFirstName());
           userForm.setEmail("vk");
            userForm.setPassword("sss");
            usersService.register(userForm);

            req.getRequestDispatcher("/jsp/main.jsp").forward(req, resp);

        } catch (ClientException e) {
            e.printStackTrace();
        } catch (ApiException e) {
            e.printStackTrace();
        } /*catch (ClientException e) {
            e.printStackTrace();
        } catch (ApiException e) {
            e.printStackTrace();
        }*/
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

    }
}
