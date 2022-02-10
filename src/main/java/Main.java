
import org.hibernate.Cache;
import org.hibernate.Session;
import org.safety.library.annotations.ACL;
import org.safety.library.hibernate.SessionProvider;
import org.safety.library.initializationModule.utils.Authenticator;
import org.safety.library.models.*;
import protectedClass.Wheel;
import protectedClass.Car;
import unprotectedClass.UnprotectedClass;
import users.TestUsers;


import java.io.*;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class Main {

    @ACL
    public static List<Car> safeLySelectCar() {

        return SessionProvider.getSession().createQuery("from Car ").getResultList();
    }

    @ACL
    public static List<Wheel> safeLySelectWheel() {
        return SessionProvider.getSession().createQuery("FROM Wheel ").getResultList();
    }

    @ACL
    public static List<UnprotectedClass> safeLySelectUnprotectedClass() {
        return SessionProvider.getSession().createQuery("FROM UnprotectedClass ").getResultList();
    }

    @ACL
    public static void safelyInsert(Object o) {
        if (!SessionProvider.getSession().getTransaction().isActive()) {
            SessionProvider.getSession().beginTransaction();
        }
        SessionProvider.getSession().save(o);
        SessionProvider.getSession().getTransaction().commit();
    }

    @ACL
    public static void safelyUpdate(Object o) {
        if (!SessionProvider.getSession().getTransaction().isActive()) {
            SessionProvider.getSession().beginTransaction();
        }
        SessionProvider.getSession().update(o);
        SessionProvider.getSession().getTransaction().commit();
    }

    @ACL
    public static void safelyDelete(Object o) {
        if (!SessionProvider.getSession().getTransaction().isActive()) {
            SessionProvider.getSession().beginTransaction();
        }
        SessionProvider.getSession().delete(o);
        SessionProvider.getSession().getTransaction().commit();
    }

    public static void main(String args[]) throws Exception {

        // Session
        Session session = SessionProvider.getSession();
        if (!SessionProvider.getSession().getTransaction().isActive()) {
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

        DefaultPrivilige defaultPrivilige1 = new DefaultPrivilige(admin, "Car", true, true, false);
        DefaultPrivilige defaultPrivilige2 = new DefaultPrivilige(pracownik, "Car", true, false, false);
        DefaultPrivilige defaultPrivilige3 = new DefaultPrivilige(admin, "Wheel", true, true, true);
        DefaultPrivilige defaultPrivilige4 = new DefaultPrivilige(pracownik, "Wheel", false, false, false);

        admin.setDefaultPriviliges(new LinkedList<>(Arrays.asList(defaultPrivilige1, defaultPrivilige3)));
        pracownik.setDefaultPriviliges(new LinkedList<>(Arrays.asList(defaultPrivilige2, defaultPrivilige4)));

        session.save(admin);
        session.save(pracownik);

        session.save(defaultPrivilige1);
        session.save(defaultPrivilige2);
        session.save(defaultPrivilige3);
        session.save(defaultPrivilige4);



        // Connection User - Role
        session.save(new UsersRole(Math.toIntExact(tomek.getId()), admin));
        session.save(new UsersRole(Math.toIntExact(ada.getId()), pracownik));
        session.save(new UsersRole(Math.toIntExact(kasia.getId()), pracownik));

        // Choose protected entities
        HibernateSelect table1 = new HibernateSelect("strangeNameWheel", "Wheel");
        HibernateSelect table2 = new HibernateSelect("strangeNameCar", "Car");
        // By default, UnprotectedClass isn't protected
        session.save(table1);
        session.save(table2);

        // Set who is able to insert data to protected entities
        AddPrivilege tomekAccess1 = new AddPrivilege(admin, "Wheel");
        AddPrivilege tomekAccess2 = new AddPrivilege(admin, "Car");
        AddPrivilege kasiaAccess = new AddPrivilege(pracownik, "Car");


        session.save(kasiaAccess);
        session.save(tomekAccess1);
        session.save(tomekAccess2);

        Wheel wheel1 = new Wheel((long)1, "size-17");
        AccessListRow accessListRow = new AccessListRow(admin, 1, "Car", true, true, true);

        session.save(accessListRow);
        session.save(wheel1);
        session.save(new Car((long)1, "BMW", wheel1));

        session.getTransaction().commit();

        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        boolean withAcl;
        String operation;

        outer:
        while (true) {
            SessionProvider.getSession().clear();
            Cache cache = SessionProvider.getSessionFactory().getCache();
            if (cache != null) {
                cache.evictAllRegions(); // Evict data from all query regions.
            }

            switchUser();
            String withAclResult = ifUseAcl();
            if(withAclResult.trim().equals("-1")){
                continue;
            }
            withAcl = Boolean.parseBoolean(withAclResult);

            operation = chooseOperation();
            if(operation.trim().equals("-1")){
                continue;
            }
            Wheel wh;

            String entity = "";
            while(true){
                System.out.println("Entity name: (1 - Car, 2 - Wheel, 3 - UnprotectedClass, 4 - AccessListRow, -1 - cancel)");
                entity = reader.readLine();
                if(!entity.trim().equals("1") && !entity.trim().equals("2") && !entity.trim().equals("3") && !entity.trim().equals("4") && !entity.trim().equals("-1")){
                    System.out.println("Wrong Input! Try Again");
                    continue;
                }
                break;
            }

            if(entity.trim().equals("-1")){
                continue;
            }


            Object obj = null;
            if (operation.equalsIgnoreCase("insert")) {
                while (true) {
                    wh = null;
                    try {
                        if (entity.equalsIgnoreCase("1")) {
                            System.out.println("Car (Long id, String brand)");
                            String data = reader.readLine();

                            System.out.println("Do you want to provide new wheel");
                            if (Boolean.parseBoolean(reader.readLine())) {
                                System.out.println("Wheel (Long id, String name)");
                                String dataWheel = reader.readLine();
                                wh = new Wheel(Long.parseLong(dataWheel.split(" ")[0]), dataWheel.split(" ")[1]);
                            } else {
                                System.out.println("Provide existing wheel id");
                                String dataWheel = reader.readLine();
                                wh = (Wheel) SessionProvider.getSession().createQuery("FROM Wheel O WHERE O.id = " + dataWheel.split(" ")[0]).getResultList().get(0);
                            }
                            obj = new Car(Long.parseLong(data.split(" ")[0]), data.split(" ")[1], wh);
                        }

                        if (entity.equalsIgnoreCase("2")) {
                            System.out.println("Wheel (Long id, String name)");
                            String data = reader.readLine();
                            obj = new Wheel(Long.parseLong(data.split(" ")[0]), data.split(" ")[1]);
                        }

                        if (entity.equalsIgnoreCase("3")) {
                            System.out.println("UnprotectedClass (Long : id, String : someValue, String : someOtherValue)");
                            String data = reader.readLine();
                            obj = new UnprotectedClass(
                                    data.split(" ")[1], data.split(" ")[2], Long.parseLong(data.split(" ")[0]));
                        }
                        if (entity.equalsIgnoreCase("4")) {
                            System.out.println("AccessList (String : role, int : protectedDataID, String : protectedTableName, Boolean: canRead, Boolean: canDelete, Boolean: canUpdate)");
                            String data = reader.readLine();
                            Role role = (Role) SessionProvider.getSession().createQuery("FROM Role R WHERE R.name = :name").setParameter("name", data.split(" ")[0]).getResultList().get(0);
                            if ((!data.split(" ")[4].equalsIgnoreCase("true") && !data.split(" ")[4].equalsIgnoreCase("false") &&
                                    !data.split(" ")[5].equalsIgnoreCase("true") && !data.split(" ")[5].equalsIgnoreCase("false") &&
                                    !data.split(" ")[6].equalsIgnoreCase("true") && !data.split(" ")[6].equalsIgnoreCase("false")) || (
                                    !data.split(" ")[2].equals("Car") && !data.split(" ")[2].equals("Wheel"))) {
                                System.out.println("Try again!");
                                continue;
                            }
                            obj = new AccessListRow(role, Integer.parseInt(data.split(" ")[1]), data.split(" ")[2], Boolean.parseBoolean(data.split(" ")[3]), Boolean.parseBoolean(data.split(" ")[4]), Boolean.parseBoolean(data.split(" ")[5]));
                        }
                    } catch (Exception e) {
                        System.out.println("Try again!");
                        continue;
                    }
                    break;
                }

                if (withAcl) {
                    try {
                        if (wh != null) {
                            safelyInsert(wh);
                        }
                        safelyInsert(obj);
                    } catch (RuntimeException e) {
                        e.printStackTrace();
                    }
                } else {
                    if (!SessionProvider.getSession().getTransaction().isActive()) {
                        SessionProvider.getSession().beginTransaction();
                    }
                    if (wh != null) {
                        SessionProvider.getSession().save(wh);
                    }
                    SessionProvider.getSession().save(obj);
                    SessionProvider.getSession().getTransaction().commit();
                }
            } else if (operation.equalsIgnoreCase("select")) {
                if (entity.equalsIgnoreCase("1")) {
                    List<Car> list = null;
                    if (withAcl) {
                        list = safeLySelectCar();
                    } else {
                        list = SessionProvider.getSession().createQuery(" FROM Car ").getResultList();
                    }
                    System.out.println("Found these records:");
                    for (Car some : list.stream().filter(e -> e.getId() != 1).toList()) {
                        System.out.println(some);
                    }
                }

                if (entity.equalsIgnoreCase("2")) {
                    List<Wheel> list = null;
                    if (withAcl) {
                        list = safeLySelectWheel();
                    } else {
                        list = SessionProvider.getSession().createQuery(" FROM Wheel ").getResultList();
                    }
                    System.out.println("Found these records:");
                    for (Wheel some : list) {
                        System.out.println(some);
                    }

                }

                if (entity.equalsIgnoreCase("3")) {
                    List<UnprotectedClass> list = null;
                    if (withAcl) {
                        list = safeLySelectUnprotectedClass();
                    } else {
                        list = SessionProvider.getSession().createQuery(" FROM UnprotectedClass ").getResultList();
                    }
                    System.out.println("Found these records:");
                    for (UnprotectedClass some : list) {
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
            } else if (operation.equalsIgnoreCase("update")) {
                System.out.println("Choose ID of record to update");
                String id = reader.readLine();


                if (entity.equalsIgnoreCase("1")) {
                    Car c = (Car) SessionProvider.getSession().createQuery("FROM Car S WHERE S.id = " + id).getResultList().get(0);
                    System.out.println("Car (String brand)");
                    String data = reader.readLine();
                    c.setBrand(data);
                    if (withAcl) {
                        try {
                            safelyUpdate(c);
                        } catch (RuntimeException e) {
                            e.printStackTrace();
                        }
                    } else {
                        if (!SessionProvider.getSession().getTransaction().isActive()) {
                            SessionProvider.getSession().beginTransaction();
                        }
                        SessionProvider.getSession().update(c);
                        SessionProvider.getSession().getTransaction().commit();
                    }
                }

                if (entity.equalsIgnoreCase("2")) {
                    System.out.println("Wheel (String name)");
                    String data = reader.readLine();
                    Wheel w = (Wheel) SessionProvider.getSession().createQuery("FROM Wheel S WHERE S.id = " + id).getResultList().get(0);
                    w.setName(data.split(" ")[0]);
                    if (withAcl) {
                        try {
                            safelyUpdate(w);
                        } catch (RuntimeException e) {
                            e.printStackTrace();
                        }
                    } else {
                        if (!SessionProvider.getSession().getTransaction().isActive()) {
                            SessionProvider.getSession().beginTransaction();
                        }
                        SessionProvider.getSession().update(w);
                        SessionProvider.getSession().getTransaction().commit();
                    }
                }

                if (entity.equalsIgnoreCase("3")) {
                    UnprotectedClass s = (UnprotectedClass) SessionProvider.getSession().createQuery("FROM UnprotectedClass S WHERE S.id = " + id).getResultList().get(0);
                    System.out.println("unprotectedClass.UnprotectedClass (String : someValue, String : someOtherValue)");
                    String data = reader.readLine();
                    s.setSomeValue(data.split(" ")[0]);
                    s.setSomeValue(data.split(" ")[1]);
                    if (withAcl) {
                        try {
                            safelyUpdate(s);
                        } catch (RuntimeException e) {
                            e.printStackTrace();
                        }
                    } else {
                        if (!SessionProvider.getSession().getTransaction().isActive()) {
                            SessionProvider.getSession().beginTransaction();
                        }
                        SessionProvider.getSession().update(s);
                        SessionProvider.getSession().getTransaction().commit();
                    }
                }
                if (entity.equalsIgnoreCase("4")) {
                    AccessListRow s = (AccessListRow) SessionProvider.getSession().createQuery("FROM AccessListRow S WHERE S.id = " + id).getResultList().get(0);
                    Role role = null;
                    String data = null;
                    while (true) {
                        System.out.println("AccessList (String : role, int : protectedDataID, String : protectedTableName, Boolean: canRead, Boolean: canDelete, Boolean: canUpdate)");
                        data = reader.readLine();

                        try {
                            role = (Role) SessionProvider.getSession().createQuery("FROM Role R WHERE R.name = :name").setParameter("name", data.split(" ")[0]).getResultList().get(0);
                        } catch (Exception e) {
                            System.out.println("Try Again!");
                            continue;
                        }

                        if ((!data.split(" ")[4].equalsIgnoreCase("true") && !data.split(" ")[4].equalsIgnoreCase("false") &&
                                !data.split(" ")[5].equalsIgnoreCase("true") && !data.split(" ")[5].equalsIgnoreCase("false") &&
                                !data.split(" ")[6].equalsIgnoreCase("true") && !data.split(" ")[6].equalsIgnoreCase("false")) || (
                                !data.split(" ")[2].equals("Car") && !data.split(" ")[2].equals("Wheel"))) {
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
                    if (withAcl) {
                        try {
                            safelyUpdate(s);
                        } catch (RuntimeException e) {
                            e.printStackTrace();
                        }
                    } else {
                        if (!SessionProvider.getSession().getTransaction().isActive()) {
                            SessionProvider.getSession().beginTransaction();
                        }
                        SessionProvider.getSession().update(s);
                        SessionProvider.getSession().getTransaction().commit();
                    }
                }
            } else if (operation.equalsIgnoreCase("delete")) {
                System.out.println("Choose ID of record to delete");
                String id = reader.readLine();

                if (entity.equalsIgnoreCase("1")) {
                    obj = SessionProvider.getSession().createQuery("FROM Car S WHERE S.id = " + id).getResultList().get(0);
                }

                if (entity.equalsIgnoreCase("2")) {
                    obj = SessionProvider.getSession().createQuery("FROM Wheel S WHERE S.id = " + id).getResultList().get(0);
                }

                if (entity.equalsIgnoreCase("3")) {
                    obj = SessionProvider.getSession().createQuery("FROM UnprotectedClass S WHERE S.id = " + id).getResultList().get(0);
                }
                if (entity.equalsIgnoreCase("4")) {
                    obj = SessionProvider.getSession().createQuery("FROM AccessListRow S WHERE S.id = " + id).getResultList().get(0);
                }

                if (withAcl) {
                    try {
                        safelyDelete(obj);
                    } catch (RuntimeException e) {
                        e.printStackTrace();
                    }
                } else {
                    if (!SessionProvider.getSession().getTransaction().isActive()) {
                        SessionProvider.getSession().beginTransaction();
                    }
                    SessionProvider.getSession().delete(obj);
                    SessionProvider.getSession().getTransaction().commit();
                }
            }
        }
    }


    public static void switchUser() throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        int userId = 0;
        System.out.println("Select User ID (1 - tomek, 2 - ada, 3 - kasia, -1 - exit)");
        String data = reader.readLine();
        while(true){
            if(data.trim().equals("-1")){
                System.exit(0);
            }
            try {
                userId = Integer.parseInt(data);
            } catch (Exception e) {
                System.out.println("Wrong input, try again");
                System.out.println("Select User ID (1 - tomek, 2 - ada, 3 - kasia, -1 - exit)");
                data = reader.readLine();
                continue;
            }
            break;
        }
        Authenticator.getInstance().setUserId(userId);
    }

    public static String ifUseAcl() {
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        System.out.println("Use ACL? (true/false), -1 - cancel");
        String string = null;
        try {
            string = reader.readLine();
        } catch (IOException e) {
            e.printStackTrace();
        }

        while (!string.equalsIgnoreCase("true") && !string.equalsIgnoreCase("false") && !string.trim().equalsIgnoreCase("-1")) {
            System.out.println("Popraw wartość: ");
            try {
                string = reader.readLine();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return string;
    }

    public static String chooseOperation() {
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        System.out.println("Specify operation?: (INSERT, SELECT, UPDATE, DELETE), -1 - cancel");
        String operation = null;
        try {
            operation = reader.readLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
        while (!operation.equalsIgnoreCase("insert") && !operation.equalsIgnoreCase("select")
                && !operation.equalsIgnoreCase("update") && !operation.equalsIgnoreCase("delete")
                && !operation.trim().equalsIgnoreCase("-1")) {
            System.out.println("Popraw wartość: ");
            try {
                operation = reader.readLine();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return operation;
    }
}
