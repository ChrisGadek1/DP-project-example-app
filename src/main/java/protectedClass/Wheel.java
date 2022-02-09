package protectedClass;

import org.safety.library.annotations.ProtectedData;

import javax.persistence.*;

@Entity
@ProtectedData(jsonPath = "")
public class Wheel {
    @Id
    @Column(name = "id", nullable = false)
    private Long id;

    public Long getId() {
        return id;
    }

    private String name;

    public Wheel(Long id, String name) {
        this.name = name;
        this.id = id;
    }

    public Wheel() {}

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "Wheel {" +
                "id=" + id +
                ", name='" + name +
                '}';
    }
}
