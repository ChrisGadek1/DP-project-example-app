
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.safety.library.annotations.ACL;
import org.safety.library.hibernate.SessionProvider;
import org.safety.library.initializationModule.Initializer;
import org.safety.library.initializationModule.utils.Authenticator;
import protectedClass.OtherProtectedClass;
import protectedClass.SomeProtectedClass1;
import unprotectedClass.UnprotectedClass;
import users.TestUsers;


import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URISyntaxException;
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


    public static void main(String args[]) throws Exception {
        Session session = SessionProvider.getSession();

        //Preparation for application use
        session.beginTransaction();
        session.createQuery("DELETE FROM SomeProtectedClass1").executeUpdate();
        session.createQuery("DELETE FROM OtherProtectedClass ").executeUpdate();
        session.createQuery("DELETE FROM TestUsers ").executeUpdate();
        TestUsers userWithRoleAdmin = new TestUsers((long) 1, "Janek Admin");
        TestUsers userWithRoleKsiegowy = new TestUsers((long) 4, "Zosia ksiegowa");
        TestUsers userWithRoleTester = new TestUsers((long) 7, "Kacper tester");
        TestUsers userWithRoleHacker = new TestUsers((long) 10, "Anna hacker");
        SomeProtectedClass1 someProtectedClass11 = new SomeProtectedClass1("wazne dane11", "inne wazne dane", (long) 1);
        SomeProtectedClass1 someProtectedClass12 = new SomeProtectedClass1("wazne dane12", "inne wazne dane", (long) 2);
        OtherProtectedClass otherProtectedClass1 = new OtherProtectedClass("wazne dane21", "inne wazne dane", (long) 1);
        OtherProtectedClass otherProtectedClass2 = new OtherProtectedClass("wazne dane22", "inne wazne dane", (long) 2);
        UnprotectedClass unprotectedClass1 = new UnprotectedClass("niewa??ne dane1", "inne niewa??ne dane", (long)1);
        UnprotectedClass unprotectedClass2 = new UnprotectedClass("niewa??ne dane2", "inne niewa??ne dane", (long)1);
        session.save(userWithRoleAdmin);
        session.save(userWithRoleKsiegowy);
        session.save(userWithRoleTester);
        session.save(userWithRoleHacker);
        session.save(someProtectedClass11);
        session.save(someProtectedClass12);
        session.save(otherProtectedClass1);
        session.save(otherProtectedClass2);
        session.save(unprotectedClass1);
        session.save(unprotectedClass2);
        session.getTransaction().commit();

        BufferedWriter writer = new BufferedWriter(new FileWriter("results.txt"));



        //Initialization of safety Library
        Initializer initializer = new Initializer();
        initializer.initialize();

        //Selection of protected Data
        //Let assume our user has role "ksiegowy". Lets try to access some protected data
        //userWithRoleKsiegowy has role "ksiegowy"

        Authenticator.getInstance().setUserId(userWithRoleKsiegowy.getId());
        List<SomeProtectedClass1> selectedData = safeLySelectSomeProtectedClass1();
        selectedData.forEach(s -> {
            try {
                writer.write(s.toString()+"\n");
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        writer.write("1===========================================1\n");

        // This role can access all rows in the database table
        // Let's try with role "hacker". User with this role doesn't have permission to read row with id = 2 in the SomeProtectedClass1 entity


        Authenticator.getInstance().setUserId(userWithRoleHacker.getId());
        selectedData = safeLySelectSomeProtectedClass1();
        selectedData.forEach(s -> {
            try {
                writer.write(s.toString()+"\n");
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        writer.write("2===========================================2\n");

        //As we can see, this user has access only to the row with id = 2, because of configuration file DataAccess1.json

        //Without @ACL annotation, the hibernate interceptor isn't invoked, so now let's try to get all rows despite lack of permissions

        selectedData = SessionProvider.getSession().createQuery("FROM SomeProtectedClass1 ").getResultList();
        selectedData.forEach(s -> {
            try {
                writer.write(s.toString()+"\n");
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        writer.write("3===========================================3\n");

        //Now lets try to save something.
        //First, create a new Protected object

        OtherProtectedClass protectedObject = new OtherProtectedClass("valuable things", "other valuable things", (long)3);

        //Let's Assume that our user has role "admin". This role has access to the create operation on OtherProtectedClass entity
        //And try to save this object

        Authenticator.getInstance().setUserId(userWithRoleAdmin.getId());
        safelyInsert(protectedObject);

        session.createQuery("FROM OtherProtectedClass ").getResultList().forEach(res -> {
            try {
                writer.write(res.toString()+"\n");
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        writer.write("4===========================================4\n");

        //As we can see, this role can add entities to the database (and has permission to read this new added field ;)).
        //Now let's try with user with role "ksiegowy". This Role doesn't have permission to add rows to the table of OtherProtectedClass entity

        Authenticator.getInstance().setUserId(userWithRoleKsiegowy.getId());
        OtherProtectedClass protectedObject1 = new OtherProtectedClass("valuable things1", "other valuable thing1", (long)4);
        try{
            safelyInsert(protectedObject1);
        }
        catch (Exception e){
            writer.write(e.getMessage()+"\n");
            writer.write("5===========================================5\n");
        }

        //As we can see, our application has thrown an exception, because this role doesn't have permission to add data to this table.
        //Now let's try with deleting. Assume we have a user, that doesn't have permission to delete some rows from database and tries to do it
        //Role "tester" doesn't have permission to delete row with id = 1 from SomeProtectedClass1 entity tester

        Authenticator.getInstance().setUserId(userWithRoleTester.getId());
        List<SomeProtectedClass1> dataToDelete = session.createQuery("FROM SomeProtectedClass1 S where id = "+(long)1).getResultList();
        try{
            safelyDelete(dataToDelete.get(0));
        }
        catch (Exception e){
            writer.write(e.getMessage()+"\n");
            writer.write("6===========================================6\n");
        }


        //As expected, when the user tries to delete this row, an exception is being thrown.
        //But... role "admin" has permission to delete this row, so lets try it again!
        //First, switch to the "admin" user:

        Authenticator.getInstance().setUserId(userWithRoleAdmin.getId());

        dataToDelete = session.createQuery("FROM SomeProtectedClass1 S where id = "+(long)1).getResultList();
        safelyDelete(dataToDelete.get(0));

        dataToDelete = session.createQuery("FROM SomeProtectedClass1").getResultList();

        dataToDelete.forEach(data -> {
            try {
                writer.write(data.toString()+"\n");
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        writer.write("7===========================================7\n");

        //As we can see, no exception is thrown and the row with id = 1 is deleted from database!
        //So... the last operation from the CRUD set is Update. Like in the previous cases, first let's try to update row with user without permissions to do it.
        //The role "ksiegowy" doesn't have permissions to update row with id = 2 in the SomeProtectedClass1 entity. Let's try to update it:

        Authenticator.getInstance().setUserId(userWithRoleKsiegowy.getId());

        List<SomeProtectedClass1> dataToUpdate = session.createQuery("FROM SomeProtectedClass1  S where S.id = "+(long)2).getResultList();

        dataToUpdate.get(0).setSomeOtherValue("haha I changed it");

        try{
            safelyUpdate(dataToUpdate.get(0));
        }
        catch (Exception e){
            writer.write(e.getMessage()+"\n");
            writer.write("8===========================================8\n");
        }

        //As expected, this user couldn't update this entity in the database.
        //We know the role "tester" can update this entity, so let's switch to this role and try again

        Authenticator.getInstance().setUserId(userWithRoleTester.getId());

        dataToUpdate = session.createQuery("FROM SomeProtectedClass1  S where S.id = "+(long)2).getResultList();

        dataToUpdate.get(0).setSomeOtherValue("haha I changed it");
        safelyUpdate(dataToUpdate.get(0));

        dataToUpdate = session.createQuery("FROM SomeProtectedClass1  S where S.id = "+(long)2).getResultList();
        dataToUpdate.forEach(s -> {
            try {
                writer.write(s.toString()+"\n");
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        writer.write("9===========================================9\n");

        writer.close();

    }
}
