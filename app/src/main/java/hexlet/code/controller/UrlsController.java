package hexlet.code.controller;

import hexlet.code.dto.BasePage;
import hexlet.code.dto.UrlPage;
import hexlet.code.dto.UrlsPage;
import hexlet.code.model.Url;
import hexlet.code.repository.UrlsRepository;
import hexlet.code.util.NamedRoutes;
import io.javalin.http.Context;
import io.javalin.http.NotFoundResponse;

import java.net.URI;
import java.net.URISyntaxException;
import java.sql.SQLException;
import java.util.Collections;
import java.util.Objects;

import static io.javalin.rendering.template.TemplateUtil.model;

public class UrlsController {
    public static void index(Context ctx) throws SQLException {
        var urls = UrlsRepository.getEntities();
        var page = new UrlsPage(urls);
        page.setFlash(ctx.consumeSessionAttribute("flash"));
        page.setFlashType(ctx.consumeSessionAttribute("flashType"));
        ctx.render("jte/url/index.jte", model("page", page));
    }

    public static void show(Context ctx) throws SQLException {
        var id = ctx.pathParamAsClass("id", Long.class).get();
        var url = UrlsRepository.find(id)
                .orElseThrow(() -> new NotFoundResponse("Entity with id = " + id + " not found"));
        var page = new UrlPage(url);
        ctx.render("jte/url/show.jte", model("page", page));
    }


    public static void create(Context ctx) throws URISyntaxException, SQLException {

        var inputUrl = ctx.formParamAsClass("url", String.class).get();
        URI parsedUrl;
        try {
            parsedUrl = new URI(inputUrl);
            if (Objects.equals(parsedUrl.getScheme(), null) || Objects.equals(parsedUrl.getAuthority(), null)) {
                throw new URISyntaxException(parsedUrl.toString(), "Некорректный URL");
            }
        } catch (URISyntaxException e) {
            var page = new BasePage("Некорректный URL", "danger");
            ctx.status(400);
            ctx.render("jte/index.jte", Collections.singletonMap("page", page));
            return;
        }
        parsedUrl = new URI(inputUrl);

        var name = parsedUrl.getScheme() + "://" + parsedUrl.getAuthority();
        Url newUrl = new Url(name);
        Url url = UrlsRepository.findByName(name).orElse(null);

        if (url != null) {
            ctx.sessionAttribute("flash", "Страница уже существует");
            ctx.sessionAttribute("flashType", "warning");
        } else {
            UrlsRepository.save(newUrl);
            ctx.sessionAttribute("flash", "Страница успешно добавлена");
            ctx.sessionAttribute("flashType", "success");
        }
        ctx.redirect(NamedRoutes.urlsPath());
    }
}
