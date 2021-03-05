import java.io.*;
import java.sql.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Date;
import java.util.Scanner;

public class MyAccount {
    Date dated=new Date();
    String date=dated.toString();
    public static double transfer=0;//Transfer Buffer for function Re-usability
    public static int TEMP=0;//Transfer Process Status
    public static int AccountNO=0;

//    AccountExist function validates the account number entered by user
    static void AccountExist(Connection co,int acc) throws SQLException{
        Statement st=co.createStatement();String sql="select * from account.account";
        ResultSet RS=st.executeQuery(sql);int temp=0;
        while (RS.next()){
            int Acc=RS.getInt("Acc_No");
            if (Acc==acc){temp=1;}
        }// if found account number then value of temp varible changed that means account no is valid for db
        if (temp==0){
            System.out.println("----- The Account No You have Entered is not Available -----");System.exit(0);
        }
    }

//    Withdraw amount from Account
    static void withdraw(Connection con) throws SQLException{ Date date=new Date();
        int acc=PIN(con); // Validate PIN for amount withdrawal
        double bal=0;
        Scanner temp=new Scanner(System.in);
        Statement stmt=con.createStatement();
        String sql="select * from account.account where Acc_No="+acc;ResultSet rs=stmt.executeQuery(sql);
        while (rs.next()){double balance=rs.getInt("Balance");bal=balance;}//Fetched Balance from DB
        AccountNO=acc;//Transfer Account No to global variable to use Transfer function
        System.out.print("Enter the Amount for Debit:");
        double amt=temp.nextDouble();transfer=amt;//Amount also transfer to global variable for Transfer function purpose
        if (amt>bal){//validation of amount value
            System.out.println("Sorry,You have low balance");
        }else {
            bal=bal-amt;//Amount deduction after validation
            sql="update account set Balance="+bal+" where Acc_No="+acc;int tempINT=stmt.executeUpdate(sql);
            System.out.println("-------------------");
            System.out.println("Final Balance:"+bal);//Display Remaining Balance
            System.out.println("-------------------");String detail="Withdraw";
            if (TEMP==1){//validates global TEMP variable value for Transfer function and passbook entry purpose
                detail="Transfer";//If Withdrawal is called for Transfer purpose then changes detail for passbook printing purpose
            }else{
                System.out.println("Your Amount Withdrawal is completed successfully");//Display only if Withdrawal function isn't called for transfer purpose
            }//Entry in Account's Pass-book
            sql="INSERT INTO passbook"+acc+" (DateTime, Detail, Debit, Balance) VALUES ('"+date+"', '"+detail+"','"+amt+"', '"+bal+"')";
            acc=stmt.executeUpdate(sql);

        }
    }

//Credit function credit amount to Account
    static void Credit(Connection con) throws SQLException{double bal=0;Date date=new Date();
        Scanner temp=new Scanner(System.in);int acc=0;Statement stmt=con.createStatement();String Name="";
        System.out.print("Enter the Account No for Credit:");acc=temp.nextInt();
        AccountExist(con,acc);//Validates that Account No exists for credit amount
        String sql="select * from account.account where Acc_No="+acc;ResultSet rs=stmt.executeQuery(sql);
        while (rs.next()){//fetches Required Details from DB
            double balance=rs.getInt("Balance");bal=balance;
            String str1=rs.getString("First_Name");String str2=rs.getString("Last_Name");
            Name=str1+" "+str2;
        }
        System.out.println("Account holder's Name:"+Name);//Display Account Holder's Name just for verification
        System.out.print("Enter The Amount for credit:");
        double amt=temp.nextDouble();//collects Amount for credit
        bal=bal+amt;//Credit Amount
        sql="update account set Balance="+bal+" where Acc_No="+acc;int tempINT=stmt.executeUpdate(sql);
        System.out.println("-------------------");
        System.out.println("Final Balance:"+bal);//Display Balance after credit
        System.out.println("-------------------");String detail="Self Deposited";
        sql="INSERT INTO passbook"+acc+" (DateTime, Detail, Credit, Balance) VALUES ('"+date+"', '"+detail+"','"+amt+"', '"+bal+"')";
        acc=stmt.executeUpdate(sql);//Pass-book Entry for Amount Credit
        System.out.println("Your Amount Credited is completed successfully");
    }

//    View function displays Account Details -Account No,Account Holder's Name,Account Balance,Place,Branch
    static void view(Connection TempCon,int acc) throws SQLException {
        Statement stm=TempCon.createStatement();ResultSet RS;
        String SQL="select * from account.account where Acc_No="+acc;RS=stm.executeQuery(SQL);
        while (RS.next()){//Fetching Account Details From DB
            int Acc_No=RS.getInt("Acc_No");
            String First_Name=RS.getString("First_Name");
            String Last_Name=RS.getString("Last_Name");
            double Balance=RS.getDouble("Balance");
            String City=RS.getString("City");
            String Branch=RS.getString("Branch");
            System.out.println("-----------------------------------------------------------------");
            System.out.println(Acc_No+"|"+First_Name+" "+Last_Name+"|"+Balance+"|"+City+"|"+Branch);//Display fetched Account Details
            System.out.println("-----------------------------------------------------------------");
        }
    }

//    Delete function is to remove Account - for Account closing
    public static void delete(Connection TeCo,int acc) throws SQLException {
        Statement st=TeCo.createStatement();
        String sq="DELETE FROM account WHERE Acc_No="+acc;//Delete Account Details from DB
        boolean b=st.execute(sq);
        if (b==false){//Validates that Account details deleted successfully
            String sql="DROP TABLE passbook"+acc;//After Successful Deletion of Details from Main Register DB then Pass-Book also removed
            int tempINT=st.executeUpdate(sql);
            System.out.println("Your Account Deleted Successfully");
        }else {
            System.out.println("Error To Delete Account");
        }
    }

// getAccountNo fetches New Account No for Create New Account
    static int getAccountNo(Connection co) throws SQLException {
        String SQL="select * from account.account";int i=0;Scanner s=new Scanner(System.in);
        int acc_no=0;Statement st=co.createStatement();ResultSet RS=st.executeQuery(SQL);
        while (RS.next()){
            i=i+1;
            int Acc_no=RS.getInt("Acc_No");
            acc_no=Acc_no+1;//Incremented Account No for Next Account No after last Account
            if (i==Acc_no){
                continue;
            }else{
                return(i);
            }
        }
        return(acc_no);//Return New Account No
    }

//    PIN function validates pin of account , In case wrong PIN entry there's total 3 chance after that program will auto-terminate
//    PIN function also return Entered Account No for further Use for different services
    static int PIN(Connection c) throws SQLException {

        Scanner s=new Scanner(System.in);int PIN=0;
        Statement st=c.createStatement();int acc=0;
        String Name="";
        System.out.print("Enter Your Account No:");acc=s.nextInt();
        AccountExist(c,acc);//validation of given Account No exists or not
        String sql="select * from account.account where Acc_No="+acc;ResultSet r=st.executeQuery(sql);
        while (r.next()){//fetches PIN from DB to verify
            int pin=r.getInt("PIN");PIN=pin;
            String str1=r.getString("First_Name");String str2=r.getString("Last_Name");
            Name=str1+" "+str2;
        }int i=0;
        while (true){
            i=i+1;
            if (i>3){//In-case 3 times continuous wrong pin entry Program will be terminated
                System.out.println("Sorry ,You have out of limit for your PIN entry");
                System.exit(0);
            }
            System.out.print("Enter Your 4-Digit PIN:");
            int tempPIN=s.nextInt();
            if (PIN==tempPIN){//Verifies Entered PIN with fetched PIN from Account-DB
                System.out.println("----- Welcome "+Name+" -----");
                return(acc);//Correct PIN entry will Pass function Verification and Returns Account No
            }
            else{
                System.out.println("You have Entered Wrong PIN.");//Wrong PIN entry
            }

        }
    }

//    4- digit pin validation
    static void chek4digit(int pin){
        if (pin>9999 || pin<999)
        { System.out.println("Your PIN must be 4 Digit only");System.exit(0);}
    }

//Create Account function to create New Account in DB
    static void CreateAccount(Connection con) throws SQLException{Date date=new Date();
        Scanner o1=new Scanner(System.in);Statement stmt=con.createStatement();
        int aNo=0;aNo=getAccountNo(con);//getAccountNo fetches Account No for Account Creation
        System.out.print("Enter Your First Name:");
        String f_name=o1.next();
        System.out.print("Enter Your Last Name:");
        String l_name=o1.next();
        System.out.print("Enter Amount for Credit:");
        double balance=o1.nextDouble();
        System.out.print("Enter Your City:");
        String city=o1.next();
        System.out.print("Enter Your Branch");
        String branch=o1.next();
        System.out.println("WARNING::Your PIN must be 4 digit.If you have entered wrong entry will be exited");
        System.out.println("Create Your Security PIN:");
        int pin=o1.nextInt();chek4digit(pin);
        System.out.println(aNo+"|"+f_name+" "+l_name+"|"+balance+"|"+city+"|"+branch);
        String sql="insert into account values ('"+aNo+"','"+f_name+"','"+l_name+"','"+balance+"','"+city+"','"+branch+"','"+pin+"')";
        int abc=stmt.executeUpdate(sql);//Creted Account - Entry in Account Register DB

        sql="CREATE TABLE passbook"+aNo+" (`DateTime` VARCHAR(45) NOT NULL,"+"`Detail` VARCHAR(45) NOT NULL,"+"`Credit` DOUBLE NULL,"+"`Debit` DOUBLE NULL,"+"`Balance` DOUBLE NOT NULL)";
        boolean bq=stmt.execute(sql);//Create New Passbook for Account
        String detail="AccountCreated";
        sql="INSERT INTO passbook"+aNo+" (DateTime, Detail, Credit, Balance) VALUES ('"+date+"', '"+detail+"', '"+balance+"', '"+balance+"')";
        abc=stmt.executeUpdate(sql);//First Passbook Entry for Amount credited at Account Creation Time
    }

//    TRANSFER function is for Amount Transfer form one Account to another Account
    static void TRANSFER(Connection con) throws SQLException{Date date=new Date();
        TEMP=1;//TEMP value displays Active Status of Transfer Process , To improve function Re_usability
        Statement st=con.createStatement();Scanner o1=new Scanner(System.in);
        double Main_Balance=0;
        withdraw(con);// Withdraw method to deduct amount from Account for transfer
        System.out.print("Enter the Account No for Credit:");
        int acc=o1.nextInt();

        String sql="select * from account.account";
        ResultSet RS=st.executeQuery(sql);int temp=0;
        while (RS.next()){
            int Acc=RS.getInt("Acc_No");
            if (Acc==acc){temp=1;}
        }
        if (temp==0){//Receiver's Account No Validation
            System.out.println("----- The Account No You have Entered is not Available -----");
            acc=AccountNO;
            sql="update passbook"+acc+" set Detail='Transfered:ErrorAccountNO' where Detail='Transfer'";
            int tempINT=st.executeUpdate(sql);//Passbook Entry For Rejected Transaction
        }
        sql="select * from account.account where Acc_No="+acc;RS=st.executeQuery(sql);
        while (RS.next()){double bal=RS.getInt("Balance");Main_Balance=bal;}
        Main_Balance=Main_Balance+transfer;//Re-credit withdrawal  Amount Back to Account due to error
        sql="update account.account set Balance="+Main_Balance+" where Acc_No="+acc;int tempINT=st.executeUpdate(sql);
        String detail;
        if (temp==0) {
             detail = "ReCredited DebitedAmount";//In-case Error for amount transfer
        }else {
             detail = "Recieved from AccountNO:" + AccountNO;//Fetched Account No of Sender
        }
        sql="INSERT INTO passbook"+acc+" (DateTime, Detail, Credit, Balance) VALUES ('"+date+"', '"+detail+"','"+transfer+"', '"+Main_Balance+"')";
        acc=st.executeUpdate(sql);//Status Update of Transaction

        sql="update passbook"+AccountNO+" set Detail='TransferedTo:"+acc+"' where Detail='Transfer'";
        int tempIN=st.executeUpdate(sql);

        if (tempINT==0){ System.out.println("Sorry for service Error To Transfer Money.Please Contact Admin");}//Re-Validation of Transaction
        else {System.out.println("    ---------- Transfer Completed Successfully ----------   "); }
        transfer=0;//Cleared Transfer amount Buffer
        TEMP=0;//Deactivate Transfer Process Status
     }

//     PassbookPrint function display pass-book (Transaction Details)
     static void PassbookPrint(Connection con) throws SQLException {
        int acc=PIN(con);//PIN verification before show display Transaction Details
        Statement stmt=con.createStatement();
        String sql="select * from passbook"+acc;
        ResultSet rs=stmt.executeQuery(sql);
        while (rs.next()){
            String DateTime=rs.getString("DateTime");
            String Detail=rs.getString("Detail");
            Double Credit=rs.getDouble("Credit");
            Double Debit=rs.getDouble("Debit");
            Double Balance=rs.getDouble("Balance");

            System.out.println("-----------------------------------------------------------------");
            System.out.println(DateTime+"|"+Detail+" | "+Credit+"|"+Debit+"|"+Balance+"|");
            System.out.println("-----------------------------------------------------------------");

        }
     }

/*------------------------------------------------------------------------------------------------------------------------------------*/
    public static void main(String[] args) throws IOException {
        Scanner o1= new Scanner(System.in);
        int Main_AccountNo=0;
        double Main_Balance=0;
        int tempINT=0;
        int acc=0;
        String sql="select * from account.account";
        String t="";
        ResultSet rs;
         try {
             String Host = "jdbc:mysql://localhost:3306/account";
             String uName = "root";
             String uPass = "root";
             Connection con = DriverManager.getConnection(Host, uName, uPass);
             Statement stmt=con.createStatement();


             while (true){
                 System.out.println("---------- Welcome ----------");
                 System.out.println("1.Withdraw");
                 System.out.println("2.Credit");
                 System.out.println("3.View");
                 System.out.println("4.Create New Account");
                 System.out.println("5.Transfer Money");
                 System.out.println("6.Correction");
                 System.out.println("7.Delete Account");
                 System.out.println("8.PassBook Printing");
                 System.out.println("0 <-- Exit");
                 System.out.println("-----------------------------\n");
                 System.out.print("Enter Your Choice:");
                 int choice=o1.nextInt();
                 if (choice==0){ System.out.println("---------- Thank You ----------");break;}

                 switch (choice){
                     case 1:/*-----Withdraw-----*/
                         withdraw(con);
                         break;

                     case 2:/*-----Credit-----*/
                         Credit(con);
                         break;

                     case 3:/*-----View/Visit-----*/
                         acc=PIN(con);view(con,acc);
                         break;

                     case 4:/*-----Create Account-----*/
                           CreateAccount(con);
                           break;

                     case 5:/*-----Transfer-----*/
                         TRANSFER(con);
                         break;
                     /*------------------------------------------------------------------------------------------------*/

                     case 6:/*-----Correction Department-----*/
                         System.out.println("-----------------------------------");
                         System.out.println(" Welcome To Correction Department ");
                         System.out.println("-----------------------------------");
                         while (true){
                             System.out.println("What would you like to correct ? ");
                             System.out.println("1.First Name");
                             System.out.println("2.Last Name");
                             System.out.println("3.City");
                             System.out.println("4.Branch");
                             System.out.println("5.PIN Change");
                             System.out.println("0 <---- Exit");
                             System.out.println("\nEnter Your choice");
                             int ch=o1.nextInt();if (ch==0){break;}
                             acc=PIN(con);//PIN verification for any details correction

                             switch (ch){
                                 case 1:/*-----First Name-----*/
                                     System.out.print("Enter New First Name:");String fn=o1.next();
                                     sql="update account set First_Name='"+fn+"' where Acc_No="+acc;tempINT=stmt.executeUpdate(sql);
                                     System.out.println("----- Your First Name Updated Successfully -----");
                                     break;

                                 case 2:/*-----Last Name-----*/
                                     System.out.print("Enter New Last Name:");String ln=o1.next();
                                     sql="update account set Last_Name='"+ln+"' where Acc_No="+acc;tempINT=stmt.executeUpdate(sql);
                                     System.out.println("----- Your Last Name Updated Successfully -----");
                                     break;

                                 case 3:/*-----City-----*/
                                     System.out.print("Enter New City Name:");String ct=o1.next();
                                     sql="update account set City='"+ct+"' where Acc_No="+acc;tempINT=stmt.executeUpdate(sql);
                                     System.out.println("----- Your City Updated Successfully -----");
                                     break;

                                 case 4:/*-----Branch-----*/
                                     System.out.print("Enter New Branch Name:");String bn=o1.next();
                                     sql="update account set Branch='"+bn+"' where Acc_No="+acc;tempINT=stmt.executeUpdate(sql);
                                     System.out.println("----- Your Branch Updated Successfully -----");
                                     break;

                                 case 5:/*-----PIN Change-----*/
                                     System.out.println("WARNING::Your PIN must be 4 digit.If you have entered wrong entry will be exited");
                                     System.out.print("Enter Your New PIN:");int p=o1.nextInt();chek4digit(p);
                                     sql="update account set PIN='"+p+"' where Acc_No="+acc;tempINT=stmt.executeUpdate(sql);
                                     System.out.println("--------Your PIN Updated Successfully--------");break;

                                 default:System.out.println("Please Enter The Valid Choice");break;
                             }acc=0;
                         }
                         break;
                     /*------------------------------------------------------------------------------------------------*/

                     case 7:/*-----Delete-----*/
                         acc=PIN(con);delete(con,acc);acc=0;
                         break;

                     case 8:/*-----PassBook Printing-----*/
                          PassbookPrint(con);
                          break;

                     default:
                         System.out.println("Enter Valid Choice");
                 }
             }
             con.close();
         }catch (SQLException err){
             System.out.println(err.getMessage());
         }
    }
}
