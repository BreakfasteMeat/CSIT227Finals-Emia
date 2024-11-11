import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class App extends JFrame{
    private JPanel pnlMain;
    private JRadioButton rbCustomer;
    private JRadioButton rbClerk;
    private JRadioButton rbManager;
    private JTextField tfName;
    private JTextArea taPersons;
    private JButton btnSave;
    private JTextField tfAge;
    private JTextField tfMonths;
    private JTextField tfSalary;
    private JButton btnClear;
    private JTextField tfLoad;
    private JButton btnLoad;
    private JButton btnSayHi;
    private JButton btnSavePerson;
    private JButton btnLoadPerson;
    private JButton btnReward;

    private List<Person> persons;
    enum PersonType{
        CUSTOMER, CLERK, MANAGER;

    }
    public PersonType stringTOPersonType(String s){
        switch(s){
            case "Customer":
                return PersonType.CUSTOMER;
            case "Clerk":
                return PersonType.CLERK;
            case "Manager":
                return PersonType.MANAGER;
        }
        return null;
    }
    public App() {
        super("App Forms");
        initWindow();
        persons = new ArrayList<>();
        btnSave.addActionListener(e -> {
            PersonType personType = getPersonType();
            Person newPerson = personFactory(personType);
            if(newPerson == null){
                return;
            }
            persons.add(newPerson);
            tfAge.setText("");
            tfSalary.setText("");
            tfMonths.setText("");
            tfName.setText("");
            appendTextArea();
        });
        btnClear.addActionListener(e -> {
            tfAge.setText("");
            tfSalary.setText("");
            tfMonths.setText("");
            tfName.setText("");
        });
        btnLoad.addActionListener(e -> {
            int num;
            try{
                num = Integer.parseInt(tfLoad.getText());
                if(num > persons.size() || num < 0) throw new NumberFormatException();
            } catch (NumberFormatException exc){
                JOptionPane.showMessageDialog(null,"Please input valid number found in the list");
                return;
            }
            Person person = persons.get(num - 1);
            tfName.setText(person.getName());
            tfAge.setText(String.valueOf(person.getAge()));

            if(person instanceof Customer){
                rbCustomer.setSelected(true);
            } else if(person instanceof Manager){
                rbManager.setSelected(true);
                tfSalary.setText(String.valueOf(((Employee)person).getSalary()));
                tfMonths.setText(String.valueOf(((Employee)person).getMonths_worked()));
            } else if(person instanceof Clerk){
                rbClerk.setSelected(true);
                tfSalary.setText(String.valueOf(((Employee)person).getSalary()));
                tfMonths.setText(String.valueOf(((Employee)person).getMonths_worked()));
            }
        });
        btnSayHi.addActionListener(e -> {
            for(Person p : persons){
                System.out.println(p);
            }
        });
        btnReward.addActionListener(e -> {
            int num;
            try{
                num = Integer.parseInt(tfLoad.getText());
                if(num > persons.size()) throw new NumberFormatException();
            } catch (NumberFormatException exc){
                tfLoad.setText("");
                JOptionPane.showMessageDialog(null,"Please input valid number found in the list");
                return;
            }
            Person person = persons.get(num - 1);
            try{
                if(person instanceof Customer) throw new IllegalArgumentException("Person selected is not an Employee");
            } catch (IllegalArgumentException exc){
                tfLoad.setText("");
                JOptionPane.showMessageDialog(null,exc.getMessage());
                return;
            }
            giveReward(num);


        });
        rbCustomer.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                tfMonths.setEditable(!rbCustomer.isSelected());
                tfSalary.setEditable(!rbCustomer.isSelected());
            }
        });
        btnSavePerson.addActionListener(e -> {
            try(BufferedWriter writer = new BufferedWriter(new FileWriter("src/Database/database.csv",false))){
                writer.write("");
                for(Person p : persons){
                    writer.append(p.getClassName() + ","+p.getName() + "," + p.getAge());
                    if(p instanceof Employee){
                        writer.append(","+((Employee) p).getMonths_worked()+ "," +((Employee) p).getSalary());
                    }
                    writer.append("\n");
                }
            } catch (IOException exc){
                System.out.println("Boohoo File Error");
            }
        });
        btnLoadPerson.addActionListener(e -> {
            try(BufferedReader reader = new BufferedReader(new FileReader(("src/Database/database.csv")))){
                String line;
                persons.clear();
                taPersons.setText("");
                while((line = reader.readLine()) != null){
                    String[] values = line.split(",");
                    if(values[0].equals("Customer")){
                        persons.add(personFactory(stringTOPersonType(values[0]),values[1], Integer.parseInt(values[2]),0,0));
                    } else {
                        persons.add(personFactory(stringTOPersonType(values[0]),values[1], Integer.parseInt(values[2]),Integer.parseInt(values[3]),Double.parseDouble(values[4])));
                    }
                    appendTextArea();
                }
            } catch (IOException exc){

            }
        });
    }

    public void appendTextArea(){
        Person person = persons.get(persons.size() - 1);
        String personType = null;
        if(person instanceof Clerk) personType = "Clerk";
        else if(person instanceof Manager) personType = "Manager";
        else if(person instanceof Customer) personType = "Customer";

        String age = String.valueOf(person.getAge());
        taPersons.append(persons.size() + ". " + personType + " - " + person.getName() + " (" + person.getAge() + ") \n");
    }
    public Person personFactory(PersonType type, String name, int age, int months_worked, double salary){
        Person person;
        switch(type){
            case CLERK:
                person = new Clerk(name,age,months_worked,salary);
                break;
            case MANAGER:
                person = new Manager(name,age,months_worked,salary);
                break;
            case CUSTOMER:
                person = new Customer(name, age);
                break;
            default:
                throw new IllegalArgumentException("Huh?");
        }
        return person;
    }
    public Person personFactory(PersonType type){
        Person person = null;
        String name;
        int age;
        int months_worked;
        double salary;
        try {
            name = tfName.getText();
            if(Objects.equals(name, "")) throw new IllegalArgumentException("Name should not be empty");
        } catch(IllegalArgumentException exc){
            JOptionPane.showMessageDialog(null,exc.getMessage());
            return null;
        }
        try{
            age = Integer.parseInt(tfAge.getText());
            if(age < 0) throw new IllegalArgumentException("Negative age is not allowed");
        } catch (NumberFormatException exc){
            JOptionPane.showMessageDialog(null,"Please input valid age");
            tfAge.setText("");
            return null;
        } catch (IllegalArgumentException exc){
            JOptionPane.showMessageDialog(null,exc.getMessage());
            tfAge.setText("");
            return null;
        }
        if(type != PersonType.CUSTOMER) {
            try {
                months_worked = Integer.parseInt(tfMonths.getText());
                if (months_worked < 0) throw new IllegalArgumentException("Negative number of months is not allowed");
            } catch (NumberFormatException exc) {
                JOptionPane.showMessageDialog(null, "Please input valid number of months");
                tfMonths.setText("");
                return null;
            } catch (IllegalArgumentException exc) {
                JOptionPane.showMessageDialog(null, exc.getMessage());
                tfMonths.setText("");
                return null;
            }
            try {
                salary = Double.parseDouble(tfSalary.getText());
                if (salary < 0) throw new IllegalArgumentException("Negative salary is not allowed");
            } catch (NumberFormatException exc) {
                JOptionPane.showMessageDialog(null, "Please input valid salary");
                tfSalary.setText("");
                return null;
            } catch (IllegalArgumentException exc) {
                JOptionPane.showMessageDialog(null, exc.getMessage());
                tfSalary.setText("");
                return null;
            }
        }
        switch(type){
            case CLERK:
                person = new Clerk(tfName.getText(), Integer.parseInt(tfAge.getText()),Integer.parseInt(tfMonths.getText()),Double.parseDouble(tfSalary.getText()));
                break;
            case MANAGER:
                person = new Manager(tfName.getText(), Integer.parseInt(tfAge.getText()),Integer.parseInt(tfMonths.getText()),Double.parseDouble(tfSalary.getText()));
                break;
            case CUSTOMER:
                person = new Customer(tfName.getText(), Integer.parseInt(tfAge.getText()));
                break;
            default:
                throw new IllegalArgumentException("Huh?");
        }
        return person;
    }
    public PersonType getPersonType(){
        if(rbCustomer.isSelected()){
            return PersonType.CUSTOMER;
        } else if(rbClerk.isSelected()){
            return PersonType.CLERK;
        } else if(rbManager.isSelected()){
            return PersonType.MANAGER;
        } else {
            System.out.println("U crazy?");
            return null;
        }
    }
    public void initWindow(){
        this.setContentPane(pnlMain);
        this.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        this.setSize(600,500);
        this.setVisible(true);

    }

    public static void main(String[] args) {
        App app = new App();
    }

    public void giveReward(int n) {
        Employee employee = (Employee) persons.get(n - 1);
        JOptionPane.showMessageDialog(null,"Thirteenth month of " + employee.getName() + ": " + employee.thirteenthmonth());
    }
}
