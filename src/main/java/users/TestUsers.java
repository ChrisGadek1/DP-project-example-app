package users;

import org.safety.library.annotations.Users;

import javax.persistence.*;

@Users(rolesListJsonPath = "RolesList.json",
        entityAccessJsonPath = "EntityAccess.json",
        rolesForUsersJsonPath = "RolesForUsers.json")
@Entity
public class TestUsers {
    @Id
    @Column(name = "id", nullable = false)
    private Long id;

    public Long getId() {
        return id;
    }

    public void setId(Long id){
        this.id = id;
    }

    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public TestUsers(Long id, String name) {
        this.name = name;
        this.id = id;
    }

    public TestUsers() {
    }
}