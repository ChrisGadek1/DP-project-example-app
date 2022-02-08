
import org.hibernate.Session;
import org.safety.library.annotations.ACL;
import org.safety.library.hibernate.SessionProvider;
import org.safety.library.initializationModule.abstractMappingObjects.EntityAccess;
import org.safety.library.initializationModule.utils.Authenticator;
import org.safety.library.models.AddPrivilege;
import org.safety.library.models.HibernateSelect;
import org.safety.library.models.Role;
import org.safety.library.models.UsersRole;
import protectedClass.OtherProtectedClass;
import protectedClass.SomeProtectedClass1;
import unprotectedClass.UnprotectedClass;
import users.TestUsers;


import java.io.*;
import java.lang.reflect.Field;
import java.util.List;

public class Main {

    @ACL
    public static List<SomeProtectedClass1> safeLySelectSomeProtectedClass1(){
        return SessionProvider.getSession().createQuery("FROM SomeProtectedClass1").getResultList();
    }

    @ACL
    public static List<SomeProtectedClass1> safeLySelectSomeProtectedClass2(){
        return SessionProvider.getSession().createQuery("FROM SomeProtectedClass1").getResultList();
    }

    @ACL
    public static void safelyInsert(Object o){
        if(!SessionProvider.getSession().getTransaction().isActive()){
            SessionProvider.getSession().beginTransaction();
        }
        SessionProvider.getSession().save(o);
        SessionProvider.getSession().getTransaction().commit();
    }

    @ACL
    public static void safelyUpdate(Object o){
        if(!SessionProvider.getSession().getTransaction().isActive()){
            SessionProvider.getSession().beginTransaction();
        }
        SessionProvider.getSession().update(o);
        SessionProvider.getSession().getTransaction().commit();
    }

    @ACL
    public static void safelyDelete(Object o){
        if(!SessionProvider.getSession().getTransaction().isActive()){
            SessionProvider.getSession().beginTransaction();
        }
        SessionProvider.getSession().delete(o);
        SessionProvider.getSession().getTransaction().commit();
    }

    @ACL
    public static List<SomeProtectedClass1> selectJoin1() {
       return SessionProvider.getSession().createQuery("select s from SomeProtectedClass1 s inner join s.someOtherValue").getResultList();
    }


    public static void main(String args[]) throws Exception {
        Session session = SessionProvider.getSession();
        if(!SessionProvider.getSession().getTransaction().isActive()){
            SessionProvider.getSession().beginTransaction();
        }

        TestUsers tomek = new TestUsers((long) 1, "tomek");
        TestUsers ada = new TestUsers((long) 2, "ada");
        TestUsers kasia = new TestUsers((long) 3, "kasia");

        session.save(tomek);
        session.save(ada);
        session.save(kasia);

        Role admin = new Role("admin");
        Role pracownik = new Role("pracownik");

        session.save(admin);
        session.save(pracownik);

        UsersRole tomekAdmin = new UsersRole(Math.toIntExact(tomek.getId()), admin);
        UsersRole adaPracownik = new UsersRole(Math.toIntExact(ada.getId()), pracownik);
        UsersRole kasiaPracownik = new UsersRole(Math.toIntExact(kasia.getId()), pracownik);

        session.save(tomekAdmin);
        session.save(adaPracownik);
        session.save(kasiaPracownik);

        AddPrivilege tomekAccess1 = new AddPrivilege(admin, "SomeProtectedClass1");
        AddPrivilege tomekAccess2 = new AddPrivilege(admin, "OtherProtectedClass");

        session.save(tomekAccess1);
        session.save(tomekAccess2);

        HibernateSelect table1 = new HibernateSelect("someprotec0_", "SomeProtectedClass1");
        HibernateSelect table2 = new HibernateSelect("otherprote0_", "OtherProtectedClass");

        OtherProtectedClass otherProtectedClass = new OtherProtectedClass("Some", "Value", (long) 1);
        session.save(otherProtectedClass);

        session.save(table1);
        session.save(table2);

        session.getTransaction().commit();


        while(true){
            BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

            System.out.println("Select User ID (1 - tomek, 2 - ada, 3 - kasia)");
            int userId = Integer.parseInt(reader.readLine());
            Authenticator.getInstance().setUserId(userId);


            System.out.println("Use ACL? (true/false)");
            String string = reader.readLine();
            boolean withAcl;
            while(!string.equalsIgnoreCase("true") && !string.equalsIgnoreCase("false")) {
                System.out.println("Popraw wartość: ");
                string = reader.readLine();
            }
            withAcl = Boolean.parseBoolean(string);

            System.out.println("Specify operation?: (INSERT, SELECT, UPDATE, DELETE)");
            String operation = reader.readLine();
            while(!operation.equalsIgnoreCase("insert") && !operation.equalsIgnoreCase("select")
                && !operation.equalsIgnoreCase("update") && !operation.equalsIgnoreCase("delete")) {
                System.out.println("Popraw wartość: ");
                operation = reader.readLine();
            }


            System.out.println("Entity name: (1 - SomeProtectedClass1, 2 - OtherProtectedClass, 3 - UnprotectedClass)");
            String entity = reader.readLine();

            Object o = null;
            if(operation.equalsIgnoreCase("insert")) {
                while (true) {
                    try {
                        if (entity.equalsIgnoreCase("1")) {
                            System.out.println("protectedClass.SomeProtectedClass1 (Long : id, String : someValue, String : someOtherValue)");
                            String data = reader.readLine();
                            o = new SomeProtectedClass1(
                                    data.split(" ")[1], data.split(" ")[2], Long.parseLong(data.split(" ")[0]), otherProtectedClass);
                        }

                        if (entity.equalsIgnoreCase("2")) {
                            System.out.println("protectedClass.OtherProtectedClass (String : someValue, String : someOtherValue)");
                            String data = reader.readLine();
                            o = new OtherProtectedClass(
                                    data.split(" ")[1], data.split(" ")[2], Long.parseLong(data.split(" ")[0]));
                        }

                        if (entity.equalsIgnoreCase("3")) {
                            System.out.println("unprotectedClass.UnprotectedClass (String : someValue, String : someOtherValue)");
                            String data = reader.readLine();
                            o = new UnprotectedClass(
                                    data.split(" ")[1], data.split(" ")[2], Long.parseLong(data.split(" ")[0]));
                        }
                    }
                    catch (Exception e){
                        System.out.println("Try again!");
                        continue;
                    }
                    break;
                }

                if(withAcl){
                    try {
                        safelyInsert(o);
                    } catch (RuntimeException e) {
                        e.printStackTrace();
                    }
                }
                else{
                    if(!SessionProvider.getSession().getTransaction().isActive()){
                        SessionProvider.getSession().beginTransaction();
                    }
                    SessionProvider.getSession().save(o);
                    SessionProvider.getSession().getTransaction().commit();
                }
            } else if(operation.equalsIgnoreCase("select")) {

                    if (entity.equalsIgnoreCase("1")) {
                       List<SomeProtectedClass1> list = selectJoin1();
                    }

                    if (entity.equalsIgnoreCase("2")) {
                        System.out.println("protectedClass.OtherProtectedClass (String : someValue, String : someOtherValue)");

                    }

                    if (entity.equalsIgnoreCase("3")) {
                        System.out.println("unprotectedClass.UnprotectedClass (String : someValue, String : someOtherValue)");
                        String data = reader.readLine();

                    }



                if(withAcl){
                    try {
                        safelyInsert(o);
                    } catch (RuntimeException e) {
                        e.printStackTrace();
                    }
                }
                else{
                    if(!SessionProvider.getSession().getTransaction().isActive()){
                        SessionProvider.getSession().beginTransaction();
                    }
                    SessionProvider.getSession().save(o);
                    SessionProvider.getSession().getTransaction().commit();
                }
            }

            System.out.println();

        }
    }
}
