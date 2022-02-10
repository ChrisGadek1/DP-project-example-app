package protectedClass;

import org.hibernate.annotations.NotFound;
import org.hibernate.annotations.NotFoundAction;
import org.safety.library.annotations.ProtectedData;

import javax.persistence.*;

@Entity
@ProtectedData(jsonPath = "")
public class Car {
    @Id
    @Column(name = "id", nullable = false)
    private Long id;

    public void setBrand(String brand) {
        this.brand = brand;
    }

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "wheel_id")
    @NotFound(action = NotFoundAction.IGNORE)
    private Wheel wheel;

    private String brand;

    public Car(Long id, String brand, Wheel wheel) {
        this.id = id;
        this.brand = brand;
        this.wheel = wheel;
    }

    public Car() {}

    @Override
    public String toString() {
        return "Car {" +
                "id=" + id +
                ", brand='" + brand +
                ", wheel="+ wheel +
                '}';
    }

    public Long getId() {
        return id;
    }

    public String getBrand() {
        return brand;
    }

    public Wheel getWheel() {
        return wheel;
    }

    public void setWheel(Wheel wheel) {
        this.wheel = wheel;
    }
}
