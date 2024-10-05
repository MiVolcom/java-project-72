package hexlet.code.controller;

import hexlet.code.model.Url;
import hexlet.code.model.UrlCheck;
import hexlet.code.repository.UrlCheckRepository;
import hexlet.code.repository.UrlRepository;
import hexlet.code.util.NamedRoutes;
import io.javalin.http.Context;
import io.javalin.http.NotFoundResponse;
import kong.unirest.HttpResponse;
import kong.unirest.Unirest;
import kong.unirest.UnirestException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import java.sql.SQLException;

public class UrlCheckController {

    public static void createCheck(Context ctx) throws SQLException {
        var urlId = ctx.pathParamAsClass("id", Long.class).getOrDefault(null);

        Url url = UrlRepository.find(urlId)
                .orElseThrow(() -> new NotFoundResponse("Url with id = \" + urlId + \" not found"));

        try {
            HttpResponse<String> response = Unirest.get(url.getName()).asString();
            Document doc = Jsoup.parse(response.getBody());

            int statusCode = response.getStatus();
            String tittle = doc.title();
            String h1 = doc.select("h1").text();
            String description = doc.select("meta[name=description]").attr("content");

            var urlCheck = new UrlCheck(urlId, statusCode, tittle, h1, description);
            UrlCheckRepository.save(urlCheck);

            ctx.sessionAttribute("flash", "Страница успешно проверена");
            ctx.sessionAttribute("flashType", "success");
            ctx.redirect(NamedRoutes.urlPath(urlId));

        } catch (UnirestException e) {
            ctx.sessionAttribute("flash", "Некорректный адрес");
            ctx.sessionAttribute("flashType", "danger");
            ctx.redirect(NamedRoutes.urlPath(urlId));

        } catch (Exception e) {
            ctx.sessionAttribute("flash", e.getMessage());
            ctx.sessionAttribute("flashType", "danger");
            ctx.redirect(NamedRoutes.urlPath(urlId));
        }
        ctx.redirect(NamedRoutes.urlPath(urlId));

    }
}
