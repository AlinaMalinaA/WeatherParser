package ru.unlimit;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.UnknownHostException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.concurrent.TimeUnit;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class Weather extends HttpServlet implements Serializable {

	private static final long serialVersionUID = 1L;
	 //захардкодить путь
	static String [] sites = { "Meteoinfo", "Mosmeteo", "Mosobl", "Cgokiev", "Meteod"};
	static String home = "C:" + File.separator + "Users" + File.separator + "Alena" + File.separator + "workspace" + File.separator + "AServlet" + File.separator;
	static String homeForLogs =  "C:" + File.separator + "Users" + File.separator + "Alena" + File.separator + "workspace" + File.separator + "AServlet" + File.separator+"logs";
	
	
	
	
	public void init() {
		System.out.println(System.getProperty("os.name"));
		if (!System.getProperty("os.name").contains("indow")) {home = "/home/webmaster/metstat/";  homeForLogs = "/home/webmaster/tomcat/apache-tomcat-8.0.47/webapps/AServlet/logs";}
		Weather.logg("The local encoding is  "+System.getProperty("file.encoding"), "init", "info"); 
		Runnable runnable = () -> {
			//System.out.println(System.getProperty("os.name"));
			String q = "__________________________________________";
		    System.out.println(q);
		    try{ 
				//Weather.ParserMeteoinfo(Weather.askRemoteServer("http://meteoinfo.ru/foptima/all.dat", "UTF-8"));
				//Weather.ParserMosmeteo(Weather.askRemoteServer("http://mosmeteo.com/sheremetevo/", "UTF-8"));
				Weather.ParserMosobl(Weather.askRemoteServer("http://meteoinfo.ru/mosobl", "UTF-8"));
				Weather.ParserCgokiev(Weather.askRemoteServer("http://cgo.kiev.ua/","windows-1251"));
				Weather.ParserMeteod(Weather.askRemoteServer("http://www.meteo.nw.ru/weather/lo_meteod.php", "windows-1251"));
				}
			catch(NullPointerException e) {
		    	System.out.println("1Ошибка Null Pointer Exception ");
		    	System.out.println(e.getMessage());
		    	Weather.logg("Ошибка Null Pointer Exception"+e, "run", "exception");
		    }
			catch(IllegalArgumentException e) {
		    	System.out.println("1Ошибка Illegal Argument Exception в ране ");
		    	System.out.println(e.toString());
		    	Weather.logg("Ошибка Illegal Argument Exception into run "+e+e.getMessage(), "run", "exception");
		    }
			catch(StringIndexOutOfBoundsException e) {
		    	System.out.println("1Ошбика парсинга в главном методе ");
		    	System.out.println(e.toString());
		    	Weather.logg("Общая ошибка парсинга в главном методе"+e, "run", "exception");
		    }
			catch(Exception e) {
		    	System.out.println("1Ошибка в run ");
		    	System.out.println(e.toString()+e.getMessage());
		    	Weather.logg("Ошибка "+e, "run", "exception");
		    }
		    while (true) {//все время
				
				try {

					run();
					TimeUnit.MINUTES.sleep(4);
				}
				catch (IllegalStateException e) {
			    	System.out.println("Ошибкуа в треде IllegalStateException ");
			        e.printStackTrace();
			        Weather.logg("Ошибка в треде "+e, "init", "exception");
			    }
				catch (InterruptedException e) {
			    	System.out.println("Ошибкуа в треде ");
			        e.printStackTrace();
			        Weather.logg("Ошибка в треде "+e, "init", "exception");
			    }
				catch (Exception e) {
			    	System.out.println("Глобальная ошибка в треде ");
			        e.printStackTrace();
			        Weather.logg("Ошибка в треде "+e, "init", "exception");
			    }
			}
		};

		Thread thread = new Thread(runnable);
		thread.start();
		
		
		File folder1 = new File(homeForLogs);
		File folder2 = new File(home);
		if (!folder1 .exists()) {
	        folder1.mkdir(); }
		if (!folder2 .exists()) {
	        folder2.mkdir(); }
	}
	
	public void main(String[] args) {
	}
	
	public void run() {
		//метеоновости НЕ парсим!!!! Их делает Артем
		Date Nowdate = new Date();
		SimpleDateFormat TimeNow = new SimpleDateFormat("HH:mm:ss ");
		SimpleDateFormat DayNow = new SimpleDateFormat("dd.yyyy.MM");
			
		File file;
			for (int i = 0; i < sites.length; i++){//на каждый сайт - проверка
				Nowdate = new Date();
				String today = DayNow.format(Nowdate);
				String Time = TimeNow.format(Nowdate);
				try {
					file = new File(home + sites[i] + File.separator + today+sites[i]+".txt");
					if(!file.exists()){//проверяем, что если файл не существует то 
						Boolean created = file.createNewFile();
						if(created){
							System.out.println("File "+file.getAbsolutePath()+" created!!!!");
							} else {System.out.println("Hueston, we have the troubles with  "+file.getAbsolutePath());}
					
					}
					long y = file.lastModified();
					long x = Nowdate.getTime() - y;//разница в миллисекундах между записанным временем и сейчас
					//System.out.println("Разница "+x);
					//System.out.println("Время у файла "+y+", разница "+x);
					switch (sites[i]){
						//где 1000 - миллисекунды, 60 - секунды, 5 - минуты
						case("Meteoinfo"): if (x > 1000*60*15 )/*каждые пятнадцать минут*/ { Weather.ParserMeteoinfo(Weather.askRemoteServer("http://meteoinfo.ru/foptima/all.dat", "UTF-8"));} else {file.setLastModified(y);} break;
						case("Mosmeteo"): if (x > 1000*60*5 )/*каждые пять минут*/ {Weather.ParserMosmeteo(Weather.askRemoteServer("http://mosmeteo.com/sheremetevo/", "UTF-8"));} else {file.setLastModified(y);} break;
						case("Mosobl"): if (x > (1000*60*60*23))/* раз в день*/ {Weather.logg("The "+sites[i]+" goes at "+Time, "run", "info");Weather.ParserMosobl(Weather.askRemoteServer("http://meteoinfo.ru/mosobl", "UTF-8"));} else {file.setLastModified(y);} break;
						case("Cgokiev"): if (x > 1000*60*10 )/*каждые 10 минут*/ {Weather.ParserCgokiev(Weather.askRemoteServer("http://cgo.kiev.ua/","windows-1251"));} else {file.setLastModified(y);} break;
						case("Meteod"): if (x > 1000*60*60*5 )/*каждые 5 часов*/ {Weather.logg("The "+sites[i]+" goes at "+Time, "run", "info");Weather.ParserMeteod(Weather.askRemoteServer("http://www.meteo.nw.ru/weather/lo_meteod.php", "windows-1251"));}  else {file.setLastModified(y);} break;
						default: file.setLastModified(y); System.out.println("Мы в дефолте ибо "+sites[i]); 
					}
						
				}
				catch(NullPointerException e) {
			    	System.out.println("Ошибка Null Pointer Exception ");
			    	System.out.println(e.getMessage());
			    	Weather.logg("Ошибка Null Pointer Exception"+e.getMessage(), "run", "exception");
			    }
				catch(IllegalArgumentException e) {
			    	System.out.println("Ошибка Illegal Argument Exception в ране ");
			    	System.out.println(e.toString());
			    	Weather.logg("Ошибка Illegal Argument Exception into run "+e+e.getMessage(), "run", "exception");
			    }
				catch(StringIndexOutOfBoundsException e) {
			    	System.out.println("Ошбика парсинга в главном методе ");
			    	System.out.println(e.toString());
			    	Weather.logg("Общая ошибка парсинга в главном методе"+e, "run", "exception");
			    }
				catch(UnknownHostException e) {
			    	System.out.println("Ошибка Unknown Host Exception  ");
			    	System.out.println(e.toString());
			    	Weather.logg("Общая Unknown Host Exception "+e, "run", "exception");
			    }
				catch(ConnectException e) {
			    	System.out.println("Ошибка соединения ");
			    	System.out.println(e.toString());
			    	Weather.logg("соединения ошибка "+e, "run", "exception");
			    }
				catch(IOException e) {
			    	System.out.println("Общая ошибка 1");
			    	System.out.println(e.toString());
			    	Weather.logg("Общая ошибка "+e+e.getMessage(), "run", "exception");
			    }
			}
	}
	
	
	
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
		//перекинуть на страницу с выбором сайтов
		//System.out.println("Получен гет запрос ");
		response.setHeader("Access-Control-Allow-Origin", "*");
		response.sendRedirect("/AServlet/weather.html");
		response.setCharacterEncoding("UTF-8");
		
	}
	
	//дупостом запрашиваются данные для веб-морды
	//возвращается строка с хтмл и данными
	public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
		//Error parsing HTTP request header
		//System.out.println("Получен пост запрос ");
		response.setContentType("text/html");
		response.setCharacterEncoding("UTF-8");
		response.setHeader("Access-Control-Allow-Origin", "*");//заголовок для разрешения запросов с других доменов
		//кусок, читающий запрос
		BufferedReader reader = request.getReader();
		String str = "";
		StringBuffer jb = new StringBuffer(256);

		Date dateNow = new Date();
		SimpleDateFormat TimeNow = new SimpleDateFormat("HH:mm:ss ");
	    String Time = TimeNow.format(dateNow);
	    
		while ((str = reader.readLine()) != null)
			jb.append(str);
		String asked = jb.toString();
		//если запрос пустой
		System.out.println("Получен запрос "+asked+" at "+Time);
		//logg("Получен запрос: "+asked, "doPost");
		if((asked.equals(""))||(asked.equals(null))||(asked.isEmpty())) {response.getWriter().println("Пустой запрос!"); response.sendRedirect("/AServlet/weather.html"); System.out.println("Пустой запроссс!!!"); }
		//иначе
		else  response.getWriter().println(MakeTheTableForBrowser(asked));
	}
	
	//читает файл и  возвращает сгенерированный хтмл с данными
	public static String MakeTheTableForBrowser(String askedsite){
		String toreturn = "";
		//System.out.println("изначальный запрос: "+askedsite);
		
		Date dateNow = new Date();
	    SimpleDateFormat format = new SimpleDateFormat("dd.yyyy.MM");
	    String q = format.format(dateNow);
		try {
			q = askedsite.substring(0, 10);
			askedsite = askedsite.substring(10, askedsite.length());
		}
		catch (StringIndexOutOfBoundsException e) {
	    	System.out.println("Ошибкуа запроса "+askedsite);
	        e.printStackTrace();
	        Weather.logg("Ошибкуа запроса "+e, "MakeTheTableForBrowser", "warning");
	    }
	    String today = q;//
	    File folder = new File(home);
		File file = new File(home +askedsite + File.separator + today + askedsite +".txt");
		Calendar calendar = new GregorianCalendar();
		
		try {
			calendar.setTime(format.parse(today));
		} catch (ParseException e1) {
			System.out.println("Ошибка парсинга даты из запроса  ");
	    	System.out.println(e1.toString());
	    	Weather.logg("Ошибка парсинга даты из запроса  "+e1.getMessage(), "MakeTheTableForBrowser", "exception");
		}
		
		//System.out.println("Для файла  "+file.getAbsolutePath()+" размер составляет "+ file.length());
		
		if (!folder.exists()) {
	        folder.mkdir(); }
		while(!file.exists()||file.length()==0){//если актуального файла нет, берем предыдущий файл
	        calendar.add(Calendar.DAY_OF_MONTH, -1);
	        String dateYesterday = format.format(calendar.getTime()); //Дата предыдущая
	        file = new File(home + askedsite + File.separator + dateYesterday + askedsite +".txt");
	        
	        toreturn = "<h4>  Внимание! Актуального файла нет, выводятся данные за "+dateYesterday +"! </h4> <br>";	
		}
		try {
			//System.out.println("Мы внутри!!");
			switch (askedsite){
				case ("Meteoinfo"): {toreturn += "<table border='1' style='width:100%;'><tr><th>Время</th><th>Температура</th><th>Точка росы</th><th>Направление ветра</th><th>Скорость ветра</th><th>Давление</th><th>Влажность</th></tr>"; break;}
				case ("Mosmeteo"): {toreturn += "<table border='1' style='width:100%;'><tr><th>Время</th><th>Температура</th><th>Точка росы</th><th>Направление ветра</th><th>Скорость ветра</th><th>Давление</th><th>Влажность</th></tr>"; break;}
				case ("Mosobl"):{ toreturn += "<table border='1' style='width:100%;'><tr><th>Станция</th><th>Tmax вчерашнего дня	</th><th>Предыдущее набл.t°С  время набл.)</th><th>Последнее набл.t°С (время набл.)</th><th>Tmin последней ночи</th><th>Осадки днем, мм	</th><th>Осадки ночью, мм</th></tr>";break;}
				case ("Cgokiev"): {toreturn += "<table border='1' style='width:100%;'><tr><th>Время</th><th>Температура</th><th>Точка росы</th><th>Направление ветра</th><th>Скорость ветра</th><th>Давление</th><th>Влажность</th><th>Макс. скорость ветра</th> </tr>";	break;}
				case ("Meteod"): {toreturn += "<table border='1' style='width:100%;'><tr><th>Город</th><th>Температура</th><th>Направление ветра</th><th>Сила ветра</th><th>Влажность</th></tr>";	break;}
				default: toreturn="Invalid name of asked site!"; break;
			}
			//Объект для чтения файла в буфер
			BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(file), "utf-8"));
			try {
				String s;
				
				int index = 0;//индекс расположения разделителя
				int columnCounter = 0;//количество столбцов
				//В цикле построчно считываем файл
				while ((s = in.readLine()) != null){
						columnCounter = 0; //счетчик столбцов обнуляется
						toreturn += "<tr>";	
						index = s.indexOf("\t\t");
						//?
						while (index!=-1){
							if (columnCounter<11){
								columnCounter++;//каждый столбец
								if (index>0){
									toreturn += "<td>"+s.substring(0, index);
									if (columnCounter>1) toreturn += "";
									toreturn += "</td>";
								}
								s = s.substring(2);
								s = s.substring(index);
								index = s.indexOf("\t");		
							}
							else {break;}
						}
						toreturn += "</tr>";
					
				}
				toreturn += "</table>";
			}
			catch(IOException e) {
		    	System.out.println("Ошибка чтения файла ");
		    	System.out.println(e.toString());
		    	logg("Ошибка чтения файла  "+e, "MakeTheTableForBrowser", "exception");
		    }
			finally {
				in.close();
			}
		}
		catch(IOException e) {
			System.out.println("Глобальная ошибка в методе чтения из файла");
			System.out.println(e.getMessage());
			logg("Глобальная ошибка в методе чтения из файла "+e, "MakeTheTableForBrowser", "exception");
		}
		
		return toreturn;
	}
	//метод делает запрос на сервер с сайтом, хранящим нужные нам данные
	//делает запрос три раза, чтобы не дудосить
	//не знаю, что будет, если трижды не придет нужный овтет
	//возвращает ответ сервера
	public static String askRemoteServer(String url, String encoding){
		StringBuffer response = null;
		
		try {
			URL obj = new URL(url);
			HttpURLConnection connection = (HttpURLConnection) obj.openConnection();
			int status =0;
			int connectionCounter = 0;//не больше трех раз
			try {
				do {
					if(connectionCounter<3){
						connection = (HttpURLConnection) obj.openConnection();
						connection.setRequestMethod("GET");	
						//connection.se
						if (status!= HttpURLConnection.HTTP_OK) status = connection.getResponseCode();//три раза спрашиваем, но до первого положительного ответа
						else break;
					}
				} while (status!= HttpURLConnection.HTTP_OK);//пока не придет положительный ответ от сервера
			}
			catch(ConnectException e) {
		    	System.out.println("Ошибка соединения ");
		    	System.out.println(e.getMessage());
		    	logg("Ошибка соединения "+e, "askRemoteServer", "exception");
		    }
			catch(IllegalArgumentException e) {
		    	System.out.println("Ошибка IllegalArgumentException в методе запроса ");
		    	System.out.println(e.getMessage());
		    	logg("Ошибка IllegalArgumentException ", "askRemoteServer", "exception");
		    }
			catch(IOException e) {
		    	System.out.println("Общая ошибка 2");
		    	System.out.println(e.getMessage());
		    	logg("Общая ошибка "+e, "askRemoteServer", "exception");
		    }
			try{	
				if(status== HttpURLConnection.HTTP_OK){//если пришел
					BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream(), encoding));
					String inputLine;
					response = new StringBuffer();
					while ((inputLine = in.readLine()) != null)
					    response.append(inputLine);
					in.close();
				}
				else {System.out.println("Сервер не отвечает по адресу "+url); logg("Сервер не отвечает по адресу "+url, "askRemoteServer", "achtung");}
			}
			catch(NullPointerException e) {
		    	System.out.println("NullPointerException ");
		    	System.out.println(e.toString());
		    	logg("Ошибка NullPointerException "+e, "askRemoteServer", "exception");
		    }
			catch(IOException e) {
		    	System.out.println("Общая ошибка 3");
		    	System.out.println(e.toString());
		    	logg("Оошибка "+e, "askRemoteServer", "exception");
		    }
		}
		catch(NullPointerException e) {
	    	System.out.println("NullPointerException ");
	    	System.out.println(e.toString());
	    	logg("Ошибка NullPointerException "+e, "askRemoteServer", "exception");
	    }
		catch(IOException e) {
	    	System.out.println("Общая ошибка 3");
	    	System.out.println(e.toString());
	    	logg("Оошибка "+e, "askRemoteServer", "exception");
	    }
		return response.toString();
	}
	
	public void askMeteonovosti() throws IOException{//не используется 
		String url = "http://meteonovosti.ru/index.php?index=8&value=26063";
		URL obj = new URL(url);
		HttpURLConnection connection = (HttpURLConnection) obj.openConnection();

		connection.setRequestMethod("GET");
		BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream(), "KOI8-R"));
		String inputLine;
		StringBuffer response = new StringBuffer();

		while ((inputLine = in.readLine()) != null) {
		    response.append(inputLine);
		}
		in.close();
		String answer = response.toString();
		ParserMeteonovosti(answer);
	}
	
	public void ParserMeteonovosti (String answer) throws IOException{//не используется 
		String SiteName="Meteonovosti";
		int ind2 = 0; //индекс конца подстроки
		Date dateNow = new Date();
		SimpleDateFormat DayNow = new SimpleDateFormat("dd.yyyy.MM");
	    String today = DayNow.format(dateNow);
	    
		StringBuilder towrite = new StringBuilder(); 
	    while (ind2!=-1) {
		    ind2 = answer.indexOf(",");//найити разделитель
			towrite.append(answer.substring(0, ind2)+ "\t\t"); //вписать в ответ
			answer = answer.substring(ind2+1);//обрезать c разделитель
			ind2 = answer.indexOf(",");
	    } 
	    towrite.append(answer);
		String file = today+SiteName+".txt";
		String title = "";
		writer(file, towrite.toString(), title, SiteName);
	}
	
	//парсит ответ от сервера Cgokiev
	public static void ParserCgokiev(String answer) {
		int ind; //индекс начала подстроки
		int ind2; //индекс конца подстроки
		String SiteName="Cgokiev";
		Date dateNow = new Date();
		SimpleDateFormat TimeNow = new SimpleDateFormat("HH:mm:ss ");
		SimpleDateFormat DayNow = new SimpleDateFormat("dd.yyyy.MM");
	    String today = DayNow.format(dateNow);
	    String Time = TimeNow.format(dateNow);
	    String towrite = "";
	    
	    ind = answer.indexOf("t:16") + 9;//сила ветра
	    answer = answer.substring(ind);
	    ind2 = answer.indexOf("</div>");//индекс конца 
		String WindStr = answer.substring(0, ind2);
		
		answer = answer.substring(5);
		ind = answer.indexOf("t:16") + 9;//сила ветра
	    answer = answer.substring(ind);
	    ind2 = answer.indexOf("</div>");//индекс конца 
	    String WindMax = answer.substring(0, ind2);
		
		answer = answer.substring(5);
		ind = answer.indexOf("0px 0px;");//направление  ветра
	    answer = answer.substring(ind);
	    ind = answer.indexOf(">") + 1;//направление  ветра
	    answer = answer.substring(ind);
	    ind2 = answer.indexOf("</td>");//индекс конца 
	    String WindDir= answer.substring(0, ind2)+" ";//нужно для красивой табуляции
		
		answer = answer.substring(5);
		ind = answer.indexOf("t:16px;")+9;//влажность
	    answer = answer.substring(ind);
	    String Humin = answer.substring(0, 2);
		
		answer = answer.substring(5);
		ind = answer.indexOf("t:16px;")+9;//температура
	    answer = answer.substring(ind);
	    ind2 = answer.indexOf("</div>");//индекс конца 
	    String Temp = answer.substring(0, ind2);
		
		answer = answer.substring(5);
		ind = answer.indexOf("t:16px;")+9;//атмосферное давление
	    answer = answer.substring(ind);
	    ind2 = answer.indexOf("</div>");//индекс конца 
	    String Pres = answer.substring(0, ind2);
		
		answer = answer.substring(5);
		ind = answer.indexOf("t:16px;")+9;//осадки за 10 минут
	    answer = answer.substring(ind);
	    ind2 = answer.indexOf("</div>");//индекс конца 
	    String  Rain = answer.substring(0, ind2)+ "";
	    
	    towrite+=	  Time  + 	"\t\t" //время
	    			+ Temp	+ 	"\t\t" 	//температура
	    			+ " - " +	"\t\t"	//точка росы
	    			+ WindDir +	"\t\t" 	//направление ветра
	    			+ WindStr +	"\t\t"	//сила ветра
	    			+ Pres 	+ 	"\t\t"	//атмосф. давление
	    			+ Humin	+ 	"\t\t"	//влажность
	    		
	    			+ WindMax + "\t\t"
	    			+ Rain  
	    			;
 	    	    
		String file = today+SiteName+".txt";
		String title = "порывы ветра\tосадки";
		writer(file, towrite, title, SiteName);
	}
	
	//парсит ответ от сервера Meteoinfo
	public static void ParserMeteoinfo(String answer) {
		String SiteName="Meteoinfo";
		int ind2 = 0; //индекс конца подстроки
		int ind1 = 0;
		Date dateNow = new Date();
		SimpleDateFormat DayNow = new SimpleDateFormat("dd.yyyy.MM");
	    String today = DayNow.format(dateNow);
	    String towrite = "";
	    int N = 96; //количество строк
	    
	    for (int i=0; i<N; i++){
	    	ind2 =answer.indexOf("]");
	    	answer = answer.substring(ind2+1);//получаем последнюю строчку
	    }
	    answer = answer.substring(2);
	    ind1 = answer.indexOf(",");
	    String Time =  answer.substring(0, ind1);//получаем дату и время 

	   //перевод юникс-времени в человекочитаемое время
	  	Calendar c = Calendar.getInstance();
	    c.setTimeInMillis(Long.valueOf(Time));
	    SimpleDateFormat formatDate = new SimpleDateFormat(" HH:mm");
	    String formatted = formatDate.format(c.getTime());
	    
	    Time = formatted	;//дата
	    
		ind2 = answer.indexOf(",");
		answer = answer.substring(ind2+1);
		ind1 = answer.indexOf(",");
		String Temp =  answer.substring(0, ind1);//температура
		answer = answer.substring(ind1+1);
		ind1 = answer.indexOf(",");
		String  Pres =  answer.substring(0, ind1) +" \t\t";//давление
		answer = answer.substring(ind1+1);
		ind1 = answer.indexOf(",");
		String  Humin =  answer.substring(0, ind1) +" \t\t";//влажность
		answer = answer.substring(ind1+1);
		ind1 = answer.indexOf(",");
		String  Rain =  answer.substring(0, ind1) +" \t\t";//осадки
		answer = answer.substring(ind1+1);
		ind1 = answer.indexOf(",");//пропуск ничего не значащего значения вида "FFFF"
		answer = answer.substring(ind1+1);
		ind1 = answer.indexOf(",");
		String WindDir =  answer.substring(0, ind1) +" \t\t";//граудсы направления
		answer = answer.substring(ind1+1);
		ind1 = answer.indexOf(",");
		String  WindStr =  answer.substring(0, ind1) +" \t\t";//скорость ветра
		answer = answer.substring(ind1+1);
		ind1 = answer.indexOf("]");
		String  WindMax =  answer.substring(0, ind1) +" \t\t";//порывы ветра
		
		towrite +=Time  +	"\t\t" //время
    			+ Temp	+ 	"\t\t" 	//температура
    			+ " - " +	"\t\t"	//точка росы
    			+ WindDir +	"\t\t" 	//направление ветра
    			+ WindStr +	"\t\t"	//сила ветра
    			+ Pres 	+ 	"\t\t"	//атмосф. давление
    			+ Humin	+ 	"\t\t"	//влажность
    		
    			+ WindMax + "\t\t"
    			+ Rain  
    			;
	 	    
		String file = today+SiteName+".txt";
		String title = "Время\tтемпература\tдавление\tвлажность\tосадки\t\tнаправление\tсила ветра\tпорывы	";
		
		writer(file, towrite, title, SiteName);
	}
	
	//парсит ответ от сервера Mosmeteo
	public static void ParserMosmeteo(String answer) {
		String SiteName="Mosmeteo";
		int ind2 = 0; //индекс конца подстроки
		int ind1 = 0;
		Date dateNow = new Date();
		SimpleDateFormat TimeNow = new SimpleDateFormat("HH:mm:ss ");
		SimpleDateFormat DayNow = new SimpleDateFormat("dd.yyyy.MM");
	    String today = DayNow.format(dateNow);
	    String Time = TimeNow.format(dateNow);
	    String towrite = "";
		try {    
			ind2 = answer.indexOf("Temperature");//найити разделитель
			answer = answer.substring(ind2);
			ind2 = answer.indexOf("<b>")+3;
			ind1 = answer.indexOf("</b>");
			String Temp = answer.substring(ind2, ind1); //вписать в ответ
			
			ind2 = answer.indexOf("Due point");//точка росы
			answer = answer.substring(ind2);
			ind2 = answer.indexOf("<b>")+3;
			ind1 = answer.indexOf("</b>");
			String  DPoint = answer.substring(ind2, ind1)+ "\t\t"; //вписать в ответ
			
			ind2 = answer.indexOf("Humidity");//найити разделитель
			answer = answer.substring(ind2);
			ind2 = answer.indexOf("<b>")+3;
			//вроде сделала
			//Exception in thread "main" java.lang.StringIndexOutOfBoundsException: String index out of range: -1
			ind1 = answer.indexOf("</b>");
			String  Humin = answer.substring(ind2, ind1)+ "\t\t"; //вписать в ответ
			
			ind2 = answer.indexOf("Comfort temperature");//найити разделитель
			answer = answer.substring(ind2);
			ind2 = answer.indexOf("<b>")+3;
			ind1 = answer.indexOf("</b>");
			String  CTemp= answer.substring(ind2, ind1)+ "\t\t"; //вписать в ответ
			
			ind2 = answer.indexOf("Wind");//найити разделитель
			answer = answer.substring(ind2);
			ind2 = answer.indexOf("<b>")+3;
			ind1 = answer.indexOf("в");
			String  WindDir= answer.substring(ind2, ind1)+ "\t\t"; //вписать в ответ
			
			ind2 = answer.indexOf("Wind");//найити разделитель
			answer = answer.substring(ind2);
			ind2 = answer.indexOf("в")+2;
			ind1 = answer.indexOf("</b>");
			String  WindStr= answer.substring(ind2, ind1)+ "\t\t"; //вписать в ответ
			
			ind2 = answer.indexOf("THW index");//найити разделитель
			answer = answer.substring(ind2);
			ind2 = answer.indexOf("<b>")+3;
			ind1 = answer.indexOf("</b>");
			String  THW= answer.substring(ind2, ind1)+ "\t\t"; //вписать в ответ
			
			ind2 = answer.indexOf("Air pressure");//найити разделитель
			answer = answer.substring(ind2);
			ind2 = answer.indexOf("<b>")+3;
			ind1 = answer.indexOf("</b>");
			String  Pres = answer.substring(ind2, ind1)+ "\t\t"; //вписать в ответ
			
			ind2 = answer.indexOf("Heat index");//найити разделитель
			answer = answer.substring(ind2);
			ind2 = answer.indexOf("<b>")+3;
			ind1 = answer.indexOf("</b>");
			String  HeatInd = answer.substring(ind2, ind1)+ "\t\t"; //вписать в ответ
			
			ind2 = answer.indexOf("Rainfall today");//найити разделитель
			answer = answer.substring(ind2);
			ind2 = answer.indexOf("<b>")+3;
			ind1 = answer.indexOf("</b>");
			String  Rain = answer.substring(ind2, ind1)+ "\t\t"; //вписать в ответ
			
			ind2 = answer.indexOf("Last rainfall");//найити разделитель
			answer = answer.substring(ind2);
			ind2 = answer.indexOf("<b>")+3;
			ind1 = answer.indexOf("</b>");
			String  LRain = answer.substring(ind2, ind1)+ "\t\t"; //вписать в ответ
			
			ind2 = answer.indexOf("Rainfall this month");//найити разделитель
			answer = answer.substring(ind2);
			ind2 = answer.indexOf("<b>")+3;
			ind1 = answer.indexOf("</b>");
			String  MRain = answer.substring(ind2, ind1)+ "\t\t"; //вписать в ответ
			
			ind2 = answer.indexOf("Rainfall this year");//найити разделитель
			answer = answer.substring(ind2);
			ind2 = answer.indexOf("<b>")+3;
			ind1 = answer.indexOf("</b>");
			String  YRain = answer.substring(ind2, ind1)+ "\t\t"; //вписать в ответ
			
			ind2 = answer.indexOf("Speed of the current rainfall");//найити разделитель
			answer = answer.substring(ind2);
			ind2 = answer.indexOf("<b>")+3;
			ind1 = answer.indexOf("</b>");
			String  SRain = answer.substring(ind2, ind1)+ "\t\t"; //вписать в ответ
			
			ind2 = answer.indexOf("Solar radiation");//найити разделитель
			answer = answer.substring(ind2);
			ind2 = answer.indexOf("<b>")+3;
			ind1 = answer.indexOf("</b>");
			String  Radiat = answer.substring(ind2, ind1)+ "\t\t"; //вписать в ответ
			
			ind2 = answer.indexOf("UV index");//найити разделитель
			answer = answer.substring(ind2);
			ind2 = answer.indexOf("<b>")+3;
			ind1 = answer.indexOf("</b>");
			String  UV = answer.substring(ind2, ind1)+ "\t\t\n"; //вписать в ответ
			towrite +=Time    + "\t\t" //время
	    			+ Temp	  +	"\t\t" 	//температура
	    			+ DPoint  +	"\t\t"	//точка росы
	    			+ WindDir +	"\t\t" 	//направление ветра
	    			+ WindStr +	"\t\t"	//сила ветра
	    			+ Pres 	  + "\t\t"	//атмосф. давление
	    			+ Humin	  + "\t\t"	//влажность
	    		
	    			+ CTemp	 + 	"\t\t"	//комфортная температура
	    			+ THW 	 + 	"\t\t"	//ТХВ индекс
	    			+ HeatInd+ 	"\t\t" 	//индекс тепла
	    			+ Rain 	 + 	"\t\t"	//осадки сейчас
	    			+ LRain  + 	"\t\t"	//последние осадки 
	    			+ MRain  + 	"\t\t"	//осадки за последний месяц
	    			+ YRain  + 	"\t\t" 	//осадки за год
	    			+ SRain  + 	"\t\t"	//скорость осадков
	    			+ Radiat + 	"\t\t" 	//Солнечн. излуч.
	    			+ UV  	 + 	"\t\t"	//УФ индекс
	    			;
			
			String file = today+SiteName+".txt";
			String title = "Темп. комфорта\tTHW индекс\tИндекс тепла\tОсадки сегодня\tПоследн. осадки\tОсадки в месяце\tГодовые осадки\t\tСкорость осадков\tСолнечн. излуч.\tУФ индекс ";
			
			writer(file, towrite, title, SiteName);
		}	
		catch (StringIndexOutOfBoundsException e) {
			System.out.println("Ошибкa парсинга в методе ParserMosmeteo "+e);
			logg("Ошибкa парсинга в методе ParserMosmeteo "+e, "ParserMosmeteo", "exception");
		}
		catch (RuntimeException e) {
			System.out.println("Общая ошибка 4 "+e);
			logg("Общая ошибка "+e, "ParserMosmeteo", "exception");
		}		
	}
	
	//парсит ответ от сервера Mosobl
	public static void ParserMosobl(String answer){
		//Weather.logg("Example of Mosobl (right after the server answer) "+answer.substring(80, 100), "ParserMosobl", "info");
		System.out.println("Пошли парсить мособласть ");
		String SiteName="Mosobl";
		int ind2 = 0; //индекс конца подстроки
		int ind1 = 0;
		Date dateNow = new Date();
		SimpleDateFormat DayNow = new SimpleDateFormat("dd.yyyy.MM");
	    String today = DayNow.format(dateNow);
	    
	    StringBuilder towrite = new StringBuilder(); 
	    String file = today+SiteName+".txt";
		
	    //System.out.println("парсим мособл "+answer);
		try {
		    for (int j=0; j<21; j++){
			    ind2 = answer.indexOf(": left\"");//найити название города 
			    answer = answer.substring(ind2+8);//обрезаем по название
				ind2 = answer.indexOf("</td>");//конец названия
				towrite.append(answer.substring(0, ind2)+ "\t\t"); //вписать в ответ название
				
				for (int i=0; i<6; i++){
					ind1 = answer.indexOf("acell");//поиск 1 значения
					if (i==2){
						answer = answer.substring(ind1+9);//обрезаем по i-тое значение
						ind1 = answer.indexOf("</b>");//поиск конца значения
						towrite.append(answer.substring(0, ind1)+ "\t\t"); //вписать в ответ
						answer = answer.substring(ind1);
					}
					else {
						answer = answer.substring(ind1+6);//обрезаем по i-тое значение
						ind1 = answer.indexOf("</td>");//поиск конца значения
						towrite.append(answer.substring(0, ind1)+ "\t\t"); //вписать в ответ
						answer = answer.substring(ind1);
					}
				}
				writer(file, towrite.toString(), "", SiteName);//записываем построчно
				//System.out.println(j + " Для записи  "+towrite);
				towrite.delete(0, towrite.length()); //обнуляем переменную для записи
		    }	
		}catch (StringIndexOutOfBoundsException e) {
			System.out.println("Ошибкa парсинга 1 "+e.getMessage());
			logg("Ошибкa  "+e, "ParserMosobl", "exception");
		}
		catch (IllegalArgumentException e) {
			System.out.println("Ошибкa парсинга ???!!??  "+e.getMessage());
			logg("Ошибкa  "+e, "ParserMosobl", "exception");
		}
		catch (RuntimeException e) {
			System.out.println("Общая ошибка 5 "+e.getMessage());
			logg("Ошибка "+e, "ParserMosobl", "exception");
		}		
	}
	
	//парсит ответ от сервера Meteod
	public static void ParserMeteod(String answer){
		//Weather.logg("Example of Meteod "+answer.substring(100, 120), "ParserMeteod", "info");
		String SiteName="Meteod";
		int ind2 = 0; //индекс конца подстроки
		int ind1 = 0;
		Date dateNow = new Date();
		SimpleDateFormat DayNow = new SimpleDateFormat("dd.yyyy.MM");
	    String today = DayNow.format(dateNow);
	    
	    String file = today+SiteName+".txt";
		writer(file, today, "", SiteName);
		
		StringBuilder towrite = new StringBuilder(); 
	    
	    ind2 = answer.indexOf("bubble':");
	    
	    while (ind2!=-1) {
	    	answer = answer.substring(ind2);
		    ind2 = answer.indexOf("<b>");
	    	answer = answer.substring(ind2+3);
	    	ind1 = answer.indexOf("</b>");
	    	towrite.append(answer.substring(0, ind1)+"  "+" \t\t");//название
	    	answer = answer.substring(ind1);
	    	//
	    	ind1 = answer.indexOf("<i>");
			answer = answer.substring(ind1+3);
			ind1 = answer.indexOf("</i>");
			towrite.append(answer.substring(0, ind1)+ " \t\t");//температура
			answer = answer.substring(ind1+3);
			//
			ind1 = answer.indexOf("<i>");
			answer = answer.substring(ind1+3);
			ind1 = answer.indexOf(",");
			towrite.append(answer.substring(0, ind1)+ " \t\t");//направление
			answer = answer.substring(ind1+1);
			//
			ind1 = answer.indexOf("</i>");
			towrite.append(answer.substring(0, ind1)+ " \t\t");//сила
			answer = answer.substring(ind1+3);
			//
			ind1 = answer.indexOf("<i>");
			answer = answer.substring(ind1+3);
			ind1 = answer.indexOf("</i>");
			towrite.append(answer.substring(0, ind1)+ " \t\t");//давление
			answer = answer.substring(ind1+3);
			//
			ind2 = answer.indexOf("bubble':");
	    	
			writer(file, towrite.toString(), "", SiteName);//записываем построчно
			towrite.delete(0, towrite.length()); //обнуляем переменную для записи
	    }
	}
	
	//пишет в файл строку
	public static  void writer(String FileName, String str, String title, String SiteName) {
	    File fileMain = new File(home + SiteName+ File.separator +FileName);
		File folder = new File(home + SiteName);
		
		//заголовок и разделитель - для удобства
		String T = "время\t \t\tтемпература\tточка росы\t\t\tнаправ. ветра\t\t\tсила ветра\t\t\tатм. давление\t\t\t\t\tвлажность\t";
		String hr = "_______________________________________________________________________________________________________________________________________________________________"; 
		
	    Boolean IsNew = false;
	    
	    if (!folder.exists()) {
	        folder.mkdir(); }
	    try {
	    	
		   	if(!fileMain.exists()){//проверяем, что если файл не существует то создаем его
		   		fileMain.createNewFile();
		        System.out.println("Создали файл " + fileMain );
		        logg("Создали файл " + fileMain.getAbsolutePath(), "writer", "warning");
		        IsNew = true;//если файл новый, ему просто запишется заголовок
		    }
		   	
		   	//BufferedWriter outForMain = new BufferedWriter(new OutputStreamWriter((), "<encoding name>"));
		    BufferedWriter outForMain = new BufferedWriter(new FileWriter(fileMain, true));//дописывание в конец документа
		    try
		    {//у мособласти и метеоданных просто подругому запись идет - раз в день
		    	//поэтому им стандартный заголовок не подойдет 
		    	//у них данные не такие
		       if (IsNew) {
		        	if((SiteName!="Mosobl")&&(SiteName!="Meteod")) {outForMain.append(T + title); outForMain.newLine(); outForMain.append(hr); outForMain.newLine();} else { }
		        }
		        	outForMain.append(str);
		        	outForMain.newLine();
		        } finally {
		        	outForMain.close();
		        }
		} catch(IOException e) {
		   logg("Общая ошибка  "+ e.toString(), "writer", "exception");
		}
	}
	
	//метод для логгирования
	//получает строку для записи и строку
	//хранящую метод, в котором вызвалось логгирование
	public static void logg (String towrite, String method, String level) {
		Date dateNow = new Date();
		SimpleDateFormat TimeNow = new SimpleDateFormat("HH:mm:ss ");
		SimpleDateFormat Day = new SimpleDateFormat("dd.yyyy.MM");
	    SimpleDateFormat DayNow = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss ");
	    Day = new SimpleDateFormat("dd.MM.yyyy");
	    String today = DayNow.format(dateNow);
	    String Time = TimeNow.format(dateNow);
	    String day = Day.format(dateNow);
	    File file = new File(homeForLogs + File.separator + "logs"+day+".html");
	    String color;
	    switch (level){
	    	case ("info"): color="green"; break;
	    	case ("warning"): color="fuchsia"; break;
	    	case ("exception"): color="orange"; break;
	    	case ("achtung"): color="red"; break;
	    	default: color="black";
	    	
	    }
		
	   // System.out.println("путь (из лога) "+file.getAbsolutePath());
		
		
		Boolean IsNew = false;	
		File folder = new File(homeForLogs);
		
		System.out.println("Пишем лог "+Time);
		
		boolean created;
		try {
			if (!folder.exists()) {
		        folder.mkdir(); }
			if(!file.exists()){//проверяем, что если файл не существует то создаем его
				created = file.createNewFile();
				if(created){
					System.out.println("!Файл "+file.getAbsolutePath()+" создан при чтении!!!!");
					IsNew = true;}
			}
			
			String header = "<!DOCTYPE HTML PUBLIC '-//W3C//DTD HTML 4.01 Transitional//EN'>"
							+"<meta http-equiv='Content-Type' content='text/html; charset=utf-8' />"
							+"<html>"
							+"	<head>"
							+"		<meta charset='utf-8'>"
							+"		<title>Логи</title>"
							+"	</head>"
							+"	<body>"
							+"	<H2>"+today+"</H2>"
							+"	<table  border='1'>";
			
			BufferedWriter writr = new BufferedWriter(new FileWriter(file, true));//дописывание в конец документа
			 try {	
				if (IsNew) {
					writr.append(header); IsNew = false;
					writr.append("<td>"+Time+"</td>");
				 	writr.append("<td>"+"Операционная система "+System.getProperty("os.name")+"</td>");
				 	writr.append("<td>"+"logg"+"</td></tr>");
				 	writr.append("<td>"+Time+"</td>");
				 	writr.append("<td>"+"Path for files is : "+ home+"</td>");
				 	writr.append("<td>"+"logg"+"</td></tr>");
				 	}
			 	writr.append("<tr style='color:"+color+";'><td>"+Time+"</td>");
			 	writr.append("<td>"+towrite+"</td>");
			 	writr.append("<td>"+method+"</td></tr>");
			 	writr.newLine();
			 	}
			 catch(IOException e) {
				   System.out.println("Ошибка записи в лог 1");
				   System.out.println(e.toString());
				   //я не знаю, что делать, если оно вызовет ошибку
			 }
			finally {
				writr.close();}
		}  
		catch(IOException e) {
			//я не знаю, что делать, если оно вызовет ошибку
			System.out.println("Ошибка записи в лог 2");
			System.out.println(e.toString());
		}
	}
}