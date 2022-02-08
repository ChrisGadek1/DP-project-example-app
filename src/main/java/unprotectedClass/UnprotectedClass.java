package unprotectedClass;

import javax.persistence.*;

@Entity
public class UnprotectedClass {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    @Column(name = "id", nullable = false)
    private Long id;

    public Long getId() {
        return id;
    }

    private String someValue;
    private String someOtherValue;

    public String getSomeValue() {
        return someValue;
    }

    public void setSomeValue(String someValue) {
        this.someValue = someValue;
    }

    public String getSomeOtherValue() {
        return someOtherValue;
    }

    public void setSomeOtherValue(String someOtherValue) {
        this.someOtherValue = someOtherValue;
    }

    public UnprotectedClass(String someValue, String someOtherValue, Long id) {
        this.someValue = someValue;
        this.someOtherValue = someOtherValue;
        this.id = id;
    }

    public UnprotectedClass() {}
}

