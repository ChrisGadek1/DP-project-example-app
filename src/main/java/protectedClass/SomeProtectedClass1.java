package protectedClass;

import org.safety.library.annotations.ProtectedData;

import org.safety.library.annotations.ProtectedData;

import javax.persistence.*;

@Entity
@ProtectedData(jsonPath = "DataAccess1.json")
public class SomeProtectedClass1 {
    @Id
    @Column(name = "id", nullable = false)
    private Long id;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public SomeProtectedClass1(String someValue, String someOtherValue, Long id) {
        this.someValue = someValue;
        this.someOtherValue = someOtherValue;
        this.id = id;
    }

    public SomeProtectedClass1() {}

    @Override
    public String toString() {
        return "SomeProtectedClass1{" +
                "id=" + id +
                ", someValue='" + someValue + '\'' +
                ", someOtherValue='" + someOtherValue + '\'' +
                '}';
    }
}
