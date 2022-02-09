
import org.hibernate.Cache;
import org.hibernate.Session;
import org.safety.library.annotations.ACL;
import org.safety.library.hibernate.SessionProvider;
import org.safety.library.initializationModule.abstractMappingObjects.EntityAccess;
import org.safety.library.initializationModule.utils.Authenticator;
import org.safety.library.models.*;
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
        return SessionProvider.getSession().createQuery("from SomeProtectedClass1 ").getResultList();
    }

    @ACL
    public static List<OtherProtectedClass> safeLySelectOtherProtectedClass(){
        return SessionProvider.getSession().createQuery("FROM OtherProtectedClass ").getResultList();
    }

    @ACL
    public static List<UnprotectedClass> safeLySelectUnprotectedClass(){
        return SessionProvider.getSession().createQuery("FROM UnprotectedClass ").getResultList();
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
        OtherProtectedClass otherProtectedClass = null;
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

        AccessListRow accessListRow = new AccessListRow(admin, 2, "SomeProtectedClass1", true, true, false);
        AccessListRow accessListRo2 = new AccessListRow(admin, 2, "OtherProtectedClass", false, false, true);

        session.save(accessListRow);
        session.save(accessListRo2);



        otherProtectedClass = new OtherProtectedClass("Some", "Value", (long) 2);
        OtherProtectedClass otherProtectedClass1 = new OtherProtectedClass("[ACCESS DENIED]", "[ACCESS DENIED]", (long)1);

        SomeProtectedClass1 someProtectedClass1 = new SomeProtectedClass1("Some", "ad", (long)2, otherProtectedClass);
        SomeProtectedClass1 someProtectedClass11 = new SomeProtectedClass1("[ACCESS DENIED]", "[ACCESS DENIED]", (long)1, null);


        session.save(someProtectedClass11);
        session.save(otherProtectedClass1);
        session.save(otherProtectedClass);
        session.save(someProtectedClass1);

        session.save(table1);
        session.save(table2);

        session.getTransaction().commit();




        while(true){
            SessionProvider.getSession().clear();
            Cache cache = SessionProvider.getSessionFactory().getCache();

            if (cache != null) {
                cache.evictAllRegions(); // Evict data from all query regions.
            }


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


            System.out.println("Entity name: (1 - SomeProtectedClass1, 2 - OtherProtectedClass, 3 - UnprotectedClass, 4 - AccessListRow)");
            String entity = reader.readLine();

            Object o = null;
            if(operation.equalsIgnoreCase("insert")) {
                while (true) {
                    try {
                        if (entity.equalsIgnoreCase("1")) {
                            System.out.println("protectedClass.SomeProtectedClass1 (Long : id, String : someValue, String : someOtherValue, int: OtherProtectedClass_ID)");
                            String data = reader.readLine();
                            otherProtectedClass = (OtherProtectedClass) SessionProvider.getSession().createQuery("FROM OtherProtectedClass O WHERE O.id = "+data.split(" ")[3]).getResultList().get(0);
                            o = new SomeProtectedClass1(
                                    data.split(" ")[1], data.split(" ")[2], Long.parseLong(data.split(" ")[0]), otherProtectedClass);
                        }

                        if (entity.equalsIgnoreCase("2")) {
                            System.out.println("protectedClass.OtherProtectedClass (Long : id, String : someValue, String : someOtherValue)");
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
                        if (entity.equalsIgnoreCase("4")) {
                            System.out.println("AccessList (String : role, int : protectedDataID, String : protectedTableName, Boolean: canRead, Boolean: canDelete, Boolean: canUpdate)");
                            String data = reader.readLine();
                            Role role = (Role) SessionProvider.getSession().createQuery("FROM Role R WHERE R.name = :name").setParameter("name", data.split(" ")[0]).getResultList().get(0);
                            if((!data.split(" ")[4].equalsIgnoreCase("true") && !data.split(" ")[4].equalsIgnoreCase("false") &&
                                    !data.split(" ")[5].equalsIgnoreCase("true") && !data.split(" ")[5].equalsIgnoreCase("false") &&
                                    !data.split(" ")[6].equalsIgnoreCase("true") && !data.split(" ")[6].equalsIgnoreCase("false")) || (
                                            !data.split(" ")[2].equals("SomeProtectedClass1") && data.split(" ")[2].equals("OtherProtectedClass"))){
                                System.out.println("Try again!");
                                continue;
                            }
                            o = new AccessListRow(role, Integer.parseInt(data.split(" ")[1]), data.split(" ")[2], Boolean.parseBoolean(data.split(" ")[3]), Boolean.parseBoolean(data.split(" ")[4]), Boolean.parseBoolean(data.split(" ")[5]));
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
                    List<SomeProtectedClass1> list = null;
                    if(withAcl){
                        list = safeLySelectSomeProtectedClass1();
                   }
                    else{
                        list = SessionProvider.getSession().createQuery(" FROM SomeProtectedClass1 ").getResultList();
                    }
                    System.out.println("Found these records:");
                    for(SomeProtectedClass1 some: list){
                        System.out.println(some);
                    }
                }

                if (entity.equalsIgnoreCase("2")) {
                    List<OtherProtectedClass> list = null;
                    if(withAcl){
                        list = safeLySelectOtherProtectedClass();
                    }
                    else{
                        list = SessionProvider.getSession().createQuery(" FROM OtherProtectedClass ").getResultList();
                    }
                    System.out.println("Found these records:");
                    for(OtherProtectedClass some: list){
                        System.out.println(some);
                    }

                }

                if (entity.equalsIgnoreCase("3")) {
                    List<UnprotectedClass> list = null;
                    if(withAcl){
                        list = safeLySelectUnprotectedClass();
                    }
                    else{
                        list = SessionProvider.getSession().createQuery(" FROM UnprotectedClass ").getResultList();
                    }
                    System.out.println("Found these records:");
                    for(UnprotectedClass some: list){
                        System.out.println(some);
                    }
                }
                if (entity.equalsIgnoreCase("4")) {
                    List<AccessListRow> list = SessionProvider.getSession().createQuery("FROM AccessListRow ").getResultList();
                    System.out.println("Found these records:");
                    for (AccessListRow some : list) {
                        System.out.println(some);
                    }
                }
            }
            else if(operation.equalsIgnoreCase("update")){
                System.out.println("Choose ID of record to update");
                String id = reader.readLine();


                if (entity.equalsIgnoreCase("1")) {
                    SomeProtectedClass1 s = (SomeProtectedClass1) SessionProvider.getSession().createQuery("FROM SomeProtectedClass1 S WHERE S.id = "+id).getResultList().get(0);
                    System.out.println("protectedClass.SomeProtectedClass1 (String : someValue, String : someOtherValue, int:  OtherProtectedClassID)");
                    String data = reader.readLine();
                    OtherProtectedClass other = (OtherProtectedClass) SessionProvider.getSession().createQuery("FROM OtherProtectedClass O WHERE O.id = "+data.split(" ")[2]).getResultList().get(0);
                    s.setSomeValue(data.split(" ")[0]);
                    s.setSomeOtherValue(data.split(" ")[1]);
                    s.setOtherProtectedClass(other);
                    if(withAcl){
                        try {
                            safelyUpdate(s);
                        } catch (RuntimeException e) {
                            e.printStackTrace();
                        }
                    }
                    else{
                        if(!SessionProvider.getSession().getTransaction().isActive()){
                            SessionProvider.getSession().beginTransaction();
                        }
                        SessionProvider.getSession().update(s);
                        SessionProvider.getSession().getTransaction().commit();
                    }
                }

                if (entity.equalsIgnoreCase("2")) {
                    OtherProtectedClass s = (OtherProtectedClass) SessionProvider.getSession().createQuery("FROM OtherProtectedClass S WHERE S.id = "+id).getResultList().get(0);
                    System.out.println("protectedClass.OtherProtectedClass (String : someValue, String : someOtherValue)");
                    String data = reader.readLine();
                    s.setSomeValue(data.split(" ")[0]);
                    s.setSomeOtherValue(data.split(" ")[1]);
                    if(withAcl){
                        try {
                            safelyUpdate(s);
                        } catch (RuntimeException e) {
                            e.printStackTrace();
                        }
                    }
                    else{
                        if(!SessionProvider.getSession().getTransaction().isActive()){
                            SessionProvider.getSession().beginTransaction();
                        }
                        SessionProvider.getSession().update(s);
                        SessionProvider.getSession().getTransaction().commit();
                    }
                }

                if (entity.equalsIgnoreCase("3")) {
                    UnprotectedClass s = (UnprotectedClass) SessionProvider.getSession().createQuery("FROM UnprotectedClass S WHERE S.id = "+id).getResultList().get(0);
                    System.out.println("unprotectedClass.UnprotectedClass (String : someValue, String : someOtherValue)");
                    String data = reader.readLine();
                    s.setSomeValue(data.split(" ")[0]);
                    s.setSomeValue(data.split(" ")[1]);
                    if(withAcl){
                        try {
                            safelyUpdate(s);
                        } catch (RuntimeException e) {
                            e.printStackTrace();
                        }
                    }
                    else{
                        if(!SessionProvider.getSession().getTransaction().isActive()){
                            SessionProvider.getSession().beginTransaction();
                        }
                        SessionProvider.getSession().update(s);
                        SessionProvider.getSession().getTransaction().commit();
                    }
                }
                if (entity.equalsIgnoreCase("4")) {
                    AccessListRow s = (AccessListRow) SessionProvider.getSession().createQuery("FROM AccessListRow S WHERE S.id = "+id).getResultList().get(0);
                    Role role = null;
                    String data = null;
                    while (true){
                        System.out.println("AccessList (String : role, int : protectedDataID, String : protectedTableName, Boolean: canRead, Boolean: canDelete, Boolean: canUpdate)");
                        data = reader.readLine();

                        try{
                            role = (Role) SessionProvider.getSession().createQuery("FROM Role R WHERE R.name = :name").setParameter("name", data.split(" ")[0]).getResultList().get(0);
                        }
                        catch (Exception e){
                            System.out.println("Try Again!");
                            continue;
                        }

                        if((!data.split(" ")[4].equalsIgnoreCase("true") && !data.split(" ")[4].equalsIgnoreCase("false") &&
                                !data.split(" ")[5].equalsIgnoreCase("true") && !data.split(" ")[5].equalsIgnoreCase("false") &&
                                !data.split(" ")[6].equalsIgnoreCase("true") && !data.split(" ")[6].equalsIgnoreCase("false")) || (
                                !data.split(" ")[2].equals("SomeProtectedClass1") && !data.split(" ")[2].equals("OtherProtectedClass"))){
                            System.out.println("Try again!");
                            continue;
                        }
                        break;
                    }
                    s.setRole(role);
                    s.setProtectedDataId(Integer.parseInt(data.split(" ")[1]));
                    s.setTableName(data.split(" ")[2]);
                    s.setCanRead(Boolean.parseBoolean(data.split(" ")[3]));
                    s.setCanDelete(Boolean.parseBoolean(data.split(" ")[4]));
                    s.setCanUpdate(Boolean.parseBoolean(data.split(" ")[5]));
                    if(withAcl){
                        try {
                            safelyUpdate(s);
                        } catch (RuntimeException e) {
                            e.printStackTrace();
                        }
                    }
                    else{
                        if(!SessionProvider.getSession().getTransaction().isActive()){
                            SessionProvider.getSession().beginTransaction();
                        }
                        SessionProvider.getSession().update(s);
                        SessionProvider.getSession().getTransaction().commit();
                    }
                }
            }
            else if(operation.equalsIgnoreCase("delete")){
                System.out.println("Choose ID of record to delete");
                String id = reader.readLine();

                if (entity.equalsIgnoreCase("1")) {
                    o =  SessionProvider.getSession().createQuery("FROM SomeProtectedClass1 S WHERE S.id = "+id).getResultList().get(0);
                }

                if (entity.equalsIgnoreCase("2")) {
                    o = SessionProvider.getSession().createQuery("FROM OtherProtectedClass S WHERE S.id = "+id).getResultList().get(0);
                }

                if (entity.equalsIgnoreCase("3")) {
                    o = SessionProvider.getSession().createQuery("FROM UnprotectedClass S WHERE S.id = "+id).getResultList().get(0);
                }
                if (entity.equalsIgnoreCase("4")) {
                    o = SessionProvider.getSession().createQuery("FROM AccessListRow S WHERE S.id = "+id).getResultList().get(0);
                }

                if(withAcl){
                    try {
                        safelyDelete(o);
                    } catch (RuntimeException e) {
                        e.printStackTrace();
                    }
                }
                else{
                    if(!SessionProvider.getSession().getTransaction().isActive()){
                        SessionProvider.getSession().beginTransaction();
                    }
                    SessionProvider.getSession().delete(o);
                    SessionProvider.getSession().getTransaction().commit();
                }
            }
        }
    }
}
