package hexlet.code.model;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import java.time.Instant;


@Getter
@ToString
public final class Url {
    @Setter
    private Long id;
    @ToString.Include
    private String name;
    @Setter
    private Instant createdAt;

    public Url(String name) {
        this.name = name;
    }
}

