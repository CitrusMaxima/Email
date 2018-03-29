package com.Email;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

import sun.misc.BASE64Encoder;

/**
 * 基于Socket的简易邮件收发程序
 * @author 杨旭
 *
 * 2017年12月21日
 */
public class javaEmail {
	
	private static Socket socket;
	private static String server;							// 服务器类型
	private static String POP3Server;						// POP3服务器地址
	private static String SMTPServer;						// SMTP服务器地址
	private static int POP3Port = 110;						// POP3服务器端口号，默认为110
	private static int SMTPPort = 25;						// SMTP服务器端口号，默认为25
	private static String user = null;						// 用户名
	private static String password = null;					// 密码
	private static String msg = null;						// 获取服务器响应信息
	private static String[] result = null;					// 服务器响应信息转换成字符串数组
	private static int mailCount = 0;						// 邮箱内邮件数量
	private static Scanner in = new Scanner(System.in);		// 获取用户输入信息
	
	public static void main(String[] args) {
		
		server = getServer();		// 选择邮件服务器
		POP3Server = "pop3." + server + ".com";
		SMTPServer = "smtp." + server + ".com";
		
        hrer:while(true)
        {
            System.out.println("请选择功能：(1：发送邮件    2：查看邮件    0：退出)");
            String input = in.nextLine();
            switch (input) {
            case "1":
                sendMail();
                break;
            case "2":
                getMail();
                break;
            case "0":
                break hrer;
            default:
            	System.out.println();
            	System.out.println("输入不正确！请重新选择！");
            	System.out.println();
                break;
            }
        }
        in.close();
	}

	/**
	 * 用户选择邮件服务器
	 * @return 邮件服务器名称
	 */
	private static String getServer() {
		
		String input = null;
		String server = null;
		boolean flag = true;
		
		do {
			System.out.println("请选择邮件服务器：(1：网易163邮箱    2：搜狐邮箱    3：新浪邮箱)");
			input = in.nextLine();
			switch (input) {
			case "1":
				server = "163";
				flag = false;
				break;
			case "2":
				server = "sohu";
				flag = false;
				break;
			case "3":
				server = "sina";
				flag = false;
				break;
			default:
				System.out.println();
				System.out.println("输入不正确！请重新选择！");
				System.out.println();
				break;
			}
		} while (flag);

		return server;
	}
	
	/**
	 * 发送邮件
	 */
	private static void sendMail() {
		
		// 检查是否已经登录
		if (!checkLogin()) {
			return;
		}
		
		// 将账号和密码进行Base64位加密
		String userBase64 = new BASE64Encoder().encode(user.getBytes());
		String passwordBase64 = new BASE64Encoder().encode(password.getBytes());
		
		// 与SMTP服务器进行通信
		try {
			// 连接SMTP服务器
			socket = new Socket(SMTPServer, SMTPPort);
			InputStream inputStream = socket.getInputStream(); 
            OutputStream outputStream = socket.getOutputStream(); 
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream)); 
            PrintWriter writter = new PrintWriter(outputStream, true);
            
            // 使用账号密码登录SMTP服务器
            System.out.println();
            System.out.println("S:" + reader.readLine());
            writter.println("helo yx"); 
            System.out.println("C:helo yx");
            System.out.println("S:" + reader.readLine());
            writter.println("auth login"); 
            System.out.println("C:auth login");
            System.out.println("S:" + reader.readLine());
            writter.println(userBase64); 
            System.out.println("C:" + userBase64);
            System.out.println("S:" + reader.readLine());
            writter.println(passwordBase64);
            System.out.println("C:" + passwordBase64);
            
            // 验证账号密码是否正确
            msg = reader.readLine();
            System.out.println("S:" + msg);
            result = getResult(msg);
            if (!result[0].equals("235")) {
            	System.out.println("账号或密码错误！\n");
            	return;
            }
            System.out.println();
            
            // 输入目标邮箱
    		System.out.println("请输入目标邮箱：");
    		String receiver = in.nextLine();
    		
    		// 设置邮件头部信息
    		System.out.println();
            writter.println("mail from:<" + user +">");
            System.out.println("C:mail from:<" + user +">");
            System.out.println("S:" + reader.readLine());
            writter.println("rcpt to:<" + receiver +">");
            System.out.println("C:rcpt to:<" + receiver +">");
            System.out.println("S:" + reader.readLine());
            
            // 设置邮件内容
            writter.println("data");
            System.out.println("C:data");
            System.out.println("S:" + reader.readLine());
            writter.println("from:" + user);
            System.out.println("C:from:" + user);
            writter.println("to:" + receiver);
            System.out.println("C:to:" + receiver);
    		
            // 输入邮件标题
            System.out.println();
    		System.out.println("请输入邮件标题：");
    		String mailSubject = in.nextLine();
    		
    		writter.println("subject:" + mailSubject);
    		System.out.println();
            System.out.println("C:subject:" + mailSubject);
            writter.println("Content-Type: text/plain;charset=\"utf-8\"");
            System.out.println("C:Content-Type: text/plain;charset=\"utf-8\"");
            writter.println();
            System.out.println("C:");
    		
    		// 输入邮件正文
            System.out.println();
    		System.out.println("请输入邮件正文：(以“.”结束)");
    		String mailContent = "";
    		String mailMessage = "";
    		while (!mailContent.equals(".")) {
    			mailContent = in.nextLine();
    			mailMessage = mailMessage + "C:" + mailContent + "\r\n";
    			writter.println(mailContent);
    		}

    		System.out.println();
            System.out.println(mailMessage);
            System.out.println("S:" + reader.readLine());
            writter.println("rset");
            System.out.println("C:rset");
            System.out.println("S:" + reader.readLine());
            writter.println("quit");
            System.out.println("C:quit");
            System.out.println("S:" + reader.readLine());
            System.out.println();
            
            System.out.println("邮件发送成功！\n");
		} catch (Exception e) {
			
		}
	}
	
	/**
	 * 接收邮件
	 */
	private static void getMail() {

		// 检查是否已经登录
		if (!checkLogin()) {
			return;
		}
		
		// 与POP3服务器进行通信
		try {
			// 连接POP3服务器
			socket = new Socket(POP3Server, POP3Port);
			InputStream inputStream = socket.getInputStream(); 
            OutputStream outputStream = socket.getOutputStream(); 
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream)); 
            PrintWriter writter = new PrintWriter(outputStream, true);
            
            // 验证用户登录
            if (!checkUser(reader, writter))
            	return;
            
            boolean flag = true;
            String input = null;
            do {
				System.out.println();
				System.out.println("请选择查询信息：(1.邮件总数    2.单封邮件信息    3.所有邮件信息    0.退出)");
				input = in.nextLine();
				switch (input) {
				case "1":
					getMailCount(reader, writter);	// 获取邮箱内邮件总数
					System.out.println("邮箱中共有" + mailCount + "封邮件");
					break;
				case "2":
					getSingleMail(reader, writter);	// 获取单封邮件信息
					break;
				case "3":
					getAllMail(reader, writter);	// 获取全部邮件信息
					break;
				case "0":
					flag = false;
					break;
				default:
					System.out.println();
					System.out.println("输入不正确！请重新选择！");
					break;
				}
			} while (flag);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * 获取邮箱内邮件数量
	 * @param reader
	 * @param writter
	 * @throws IOException
	 */
	private static void getMailCount(BufferedReader reader, PrintWriter writter) throws IOException {
		
		System.out.println();
		writter.println("stat");
		System.out.println("C:stat");	// stat命令获取邮件数量信息
		msg = reader.readLine();
		System.out.println("S:" + msg);
		result = getResult(msg);
		System.out.println();
		mailCount = Integer.parseInt(result[1]);
		
	}
	
	/**
	 * 获取单封邮件内容
	 * @param mailNumber 邮件编号
	 * @param reader
	 * @param writter
	 * @return 返回邮件内容信息
	 * @throws IOException
	 */
	private static String getMail(int mailNumber, BufferedReader reader, PrintWriter writter) throws IOException {
		
		System.out.println();
		writter.println("retr " + mailNumber);
		System.out.println("C:retr " + mailNumber);
		String line = reader.readLine();
		System.out.println("S:" + line);
		result = getResult(line);
		// 无法获取邮件，即用户输入邮件编号超过邮箱内邮件数量，返回空值
		if (result[0].equals("-ERR")) {
			System.out.println();
			return "";
		}
		System.out.println();
		System.out.println("================第" + mailNumber + "封邮件================");
		String singleMail = "";
		while(!line.equalsIgnoreCase(".")) {
		    line = reader.readLine();
		    singleMail = singleMail + line + "\r\n";
		    System.out.println("S:" + line);
		}
		return singleMail;
	}
	
	/**
	 * 获取单封邮件信息
	 * @param reader
	 * @param writter
	 * @throws Exception
	 */
	private static void getSingleMail(BufferedReader reader, PrintWriter writter) throws Exception {
		
		System.out.println("请输入想获取的邮件序号：");
		String mailNumber = in.nextLine();
		for (int i = 0; i < mailNumber.length(); ++i) {
			if (!Character.isDigit(mailNumber.charAt(i))) {
				System.out.println();
				System.out.println("请输入正确的邮件序号！！");
				return;
			}
		}
		String singleMail = getMail(Integer.parseInt(mailNumber), reader, writter);
		if (singleMail.equals("")) {
			System.out.println("输入邮件序号错误！");
			return;
		}
		
		while(true) {
			System.out.println();
			System.out.println("是否下载邮件？(Y/N)");
			String input = in.nextLine();
			if (input.equals("Y") || (input.equals("y"))) {
				downloadMail(mailNumber, singleMail);
				break;
			} else if (input.equals("N") || (input.equals("n"))) {
				break;
			} else {
				System.out.println("输入错误！");
			}
		}
	}
	
	/**
	 * 获取所有邮件信息
	 * @param reader
	 * @param writter
	 * @throws Exception
	 */
	private static void getAllMail(BufferedReader reader, PrintWriter writter) throws Exception {
		
		System.out.println();
		if (mailCount == 0) {
			System.out.println("获取邮件总数...");
			getMailCount(reader, writter);
		}
		
		String[] allMail = new String[mailCount];	// 存储全部邮件信息
		for (int i = 1; i <= mailCount; ++i) {
			allMail[i-1] = getMail(i, reader, writter);
		}
		System.out.println();
		System.out.println("邮件获取完毕！");
		
		while(true) {
			System.out.println();
			System.out.println("是否下载邮件？(Y/N)");
			String input = in.nextLine();
			if (input.equals("Y") || (input.equals("y"))) {
				for (int i = 1; i <= mailCount; ++i) {
					downloadMail(String.valueOf(i), allMail[i-1]);
				}
				break;
			} else if (input.equals("N") || (input.equals("n"))) {
				break;
			} else {
				System.out.println("输入错误！");
			}
		}
	}
	
	/**
	 * 下载邮件
	 * @param mailNumber 邮件编号
	 * @param singleMail 单封邮件信息
	 * @throws Exception
	 */
	private static void downloadMail(String mailNumber, String singleMail) throws Exception {

		String filePath = "D:\\" + mailNumber + ".txt";
		contentToTxt(filePath, singleMail);
		System.out.println("第" + mailNumber + "封邮件已成功下载到D盘！保存为" + mailNumber + ".txt！");

	}
	
	/**
	 * 将服务器相应信息转换成字符串数组
	 * @param msg 服务器响应信息
	 * @return 返回字符串数组
	 */
	private static String[] getResult(String msg) {
		
		String[] result = msg.split(" ");

		return result;
	}
	
	/**
	 * 验证用户登录
	 * @param reader
	 * @param writter
	 * @return 返回boolean类型数据，true代表验证成功，false代表验证失败
	 * @throws IOException
	 */
	private static boolean checkUser(BufferedReader reader, PrintWriter writter) throws IOException {
		
		System.out.println();
		msg = reader.readLine();
		System.out.println("S:" + msg);
		result = getResult(msg);
		if (!result[0].equals("+OK")) {
			System.out.println("连接服务器失败！\n");
			return false;
		}
		
		// 使用账号密码登录POP3服务器
		writter.println("user " + user);
		System.out.println("C:user " + user);
		System.out.println("S:" + reader.readLine());
		writter.println("pass " + password);
		System.out.println("C:pass " + password);
		
		// 验证账号密码是否正确
		msg = reader.readLine();
		System.out.println("S:" + msg);
		result = getResult(msg);
		if (!result[0].equals("+OK")) {
			System.out.println();
			System.out.println("邮箱账号或密码错误！\n");
			user = null;
			password = null;
			return false;
		}
		
		return true;
	}
	
	/**
	 * 判断用户是否已经输入账号密码信息，若未输入则要求输入
	 * @return 返回boolean类型数据，true代表已经输入或输入正确，false代表邮箱格式错误
	 */
	private static boolean checkLogin() {
		
		if (user == null) {
			System.out.println("请输入邮箱账号：");
			user = in.nextLine();
		}
		int pos = user.indexOf("@");
		if (pos == -1) {
			System.out.println("邮箱账号错误！\n");
			user = null;
			return false;
		}
		
		if (password == null) {
			System.out.println("请输入邮箱密码：");
			password = in.nextLine();
		}
		return true;
	}
	
	/**
	 * 将邮件信息输入到本地txt文件中
	 * @param filePath 本地txt文件地址
	 * @param singleMail 单封邮件信息
	 * @throws Exception
	 */
	private static void contentToTxt(String filePath, String singleMail) throws Exception {
		
		File file = new File(filePath);
		FileOutputStream fos;
		try {
			// 文件不存在则新建文件
			if (!file.exists())
				file.createNewFile();
			
        	fos=new FileOutputStream(file);
        	fos.write(singleMail.getBytes());
        	fos.close();
        } catch (FileNotFoundException e) {
        	e.printStackTrace();
        } catch (IOException e) {
        	e.printStackTrace();
        }
    }

}