package ru.unlimit;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.bind.annotation.XmlElementDecl.GLOBAL;



public class Weather extends HttpServlet implements Serializable {

	static Logger logger = Logger.getLogger(Weather.class.getName());
	
	
	public static void main(String[] args) throws ParseException, IOException {
		
		try {
			FileHandler fh = new FileHandler("log\\Logger.log", true);
			logger.addHandler(fh);
			
		} catch (SecurityException e) {
			logger.log(Level.SEVERE, "Не удалось создать файл лога из-за политики безопасности.", e); 
		} catch (IOException e) {
			logger.log(Level.SEVERE, "Не удалось создать файл лога из-за ошибки ввода-вывода.", e); 
		}
		
		//_____________________________________
		//проверка пропусков в записи
		while (true) {//все время
			SimpleDateFormat format = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss ");

			//метеоновости НЕ парсим!!!! Их делает Артем
			String [] sites = { "Meteoinfo", "Mosmeteo", "Mosobl", "Cgokiev"};
			for (int i = 0; i < sites.length; i++){//на каждый сайт - проверка
				Date dateNow = new Date();
				long x = dateNow.getTime() - format.parse(readerLastModification(sites[i])).getTime();//разница в миллисекундах между записанным временем и сейчас
				switch (sites[i]){
					case("Meteoinfo"): if (x > 1000*60*15 )/*каждые пятнадцать минут*/ ParserMeteoinfo(askRemoteServer("http://meteoinfo.ru/foptima/all.dat", "UTF-8"));break;
					case("Mosmeteo"): if (x > 1000*60*5 )/*каждые пять минут*/ ParserMosmeteo(askRemoteServer("http://mosmeteo.com/sheremetevo/", "UTF-8"));break;
					case("Mosobl"): if (x > 1000*60*60*24 )/*раз в день*/ ParserMosobl(askRemoteServer("http://meteoinfo.ru/mosobl", "UTF-8"));break;
					case("Cgokiev"): if (x > 1000*60*10 )/*каждые 10 минут*/ ParserCgokiev(askRemoteServer("http://cgo.kiev.ua/","windows-1251"));break;
					default: break;
				}
			}	
		}
	}
	
	//дупостом запрашиваются данные для веб-морды
	//возвращается строка с хтмл и данными
	public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
		response.setContentType("text/html");
		response.setCharacterEncoding("UTF-8");
		//кусок, читающий запрос
		BufferedReader reader = request.getReader();
		String str = "";
		StringBuffer jb = new StringBuffer(256);
		while ((str = reader.readLine()) != null)
			jb.append(str);
		//если запрос пустой
		if((jb.toString()=="")||(jb.toString()==null)) response.getWriter().println("Пустой запрос!");
		//иначе
		else  response.getWriter().println(MakeTheTableForBrowser(jb.toString()));
	}
	
	//читает файл и  возвращает сгенерированный хтмл с данными
	public static String MakeTheTableForBrowser(String askedsite){
		String toreturn = "";
		System.out.println("Запросили следующий сайт: "+askedsite);
		//дата сейчас
		Date dateNow = new Date();
	    SimpleDateFormat format = new SimpleDateFormat("dd.yyyy.MM");
	    String today = format.format(dateNow);
		
		File file = new File("C:" + File.separator + "Users" + File.separator + "Alena" + File.separator + "workspace" + File.separator + "AServlet" + File.separator +askedsite + File.separator + today + askedsite +".txt");
		Calendar calendar = new GregorianCalendar();
		while(!file.exists()){//если актуального файла нет, берем предыдущий файл
	        calendar.add(Calendar.DAY_OF_MONTH, -1);
	        String dateYesterday = format.format(calendar.getTime()); //Дата предыдущая
	        file = new File("C:" + File.separator + "Users" + File.separator + "Alena" + File.separator + "workspace" + File.separator + "AServlet" + File.separator +askedsite + File.separator + dateYesterday + askedsite +".txt");
	        toreturn = "<br>  Внимание! Актуального файла нет, выводятся данные за "+dateYesterday +"! <br> <br>";	
		}
		try {
			switch (askedsite){
				case ("Meteoinfo"): {toreturn += "<table border='1' style='width:100%;'><tr><th>Время</th><th>Температура</th><th>Точка росы</th><th>Направление ветра</th><th>Скорость ветра</th><th>Давление</th><th>Влажность</th><th>Порывы ветра</th><th>Осадки</th></tr>"; break;}
				case ("Mosmeteo"): {toreturn += "<table border='1' style='width:100%;'><tr><th>Время</th><th>Температура</th><th>Точка росы</th><th>Направление ветра</th><th>Скорость ветра</th><th>Давление</th><th>Влажность</th><th>Комфортная температура</th><th>ТХВ индекс</th><th>Индекс тепла</th><th>Осадки сейчас</th><th>Последние осадки </th><th>Осадки за последний месяц</th><th>Осадки за год</th><th>Скорость осадков</th><th>Солнечн. излуч.</th><th>УФ индекс</th></tr>"; break;}
				case ("Mosobl"):{ toreturn += "<table border='1' style='width:100%;'><tr><th>Станция</th><th>Tmax вчерашнего дня	</th><th>Предыдущее набл.t°С  время набл.)</th><th>Последнее набл.t°С (время набл.)</th><th>	Tmin последней ночи</th><th>Осадки днем, мм	</th><th>Осадки ночью, мм</th></tr>";break;}
				case ("Cgokiev"): {toreturn += "<table border='1' style='width:100%;'><tr><th>Время</th><th>Температура</th><th>Точка росы</th><th>Направление ветра</th><th>Скорость ветра</th><th>Давление</th><th>Влажность</th><th>Порывы ветра</th></tr>";	break;}
				default: toreturn="Invalid name of asked site!"; break;
			}
			//Объект для чтения файла в буфер
			BufferedReader in = new BufferedReader(new FileReader(file));
			try {
				String s;
				int stringCounter = 0;//Количество строк
				int index = 0;//индекс расположения разделителя
				int columnCounter = 0;//количество столбцов
				//В цикле построчно считываем файл
				while ((s = in.readLine()) != null){
					stringCounter++;
					if (stringCounter>1){//после первой строки, ибо первая - заголовки
						columnCounter = 0; //счетчик столбцов обнуляется
						toreturn += "<tr>";	
						index = s.indexOf("\t\t");
						while (index!=-1){
							columnCounter++;//каждый столбец
							index = s.indexOf("\t\t");
							if (index>0){
								toreturn += "<td>"+s.substring(0, index);
								if (columnCounter>1) toreturn += "<div class='fill' style='width:10%'></div>";
								toreturn += "</td>";
							}
							s = s.substring(2);
							s = s.substring(index);
							index = s.indexOf("\t");							 	
						}
						toreturn += "</tr>";
					}
				}
				toreturn += "</table>";		
			}
			catch(IOException e) {
		    	logger.log(Level.SEVERE, "Exception reader: ", e);
		    	System.out.println("Ошибка чтения файла ");
		    	System.out.println(e.toString());
		    }
			finally {
				in.close();
			}
		}
		catch(IOException e) {
			logger.log(Level.SEVERE, "Global Exception with reader: ", e);
			System.out.println("Глобальная ошибка в методе чтения из файла");
			System.out.println(e.getMessage());
		}
		return toreturn;
	}
	
	
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException  {
		
		String From = java.net.URLDecoder.decode(request.getParameter("fromdata"), "UTF-8");//дата от которой считать
		String To = java.net.URLDecoder.decode(request.getParameter("todata"), "UTF-8");//дата до которой считать
		
		System.out.println("Запрос был "+From+" и "+To );
		
		response.setContentType("text/html");
		response.setCharacterEncoding("UTF-8");
		response.setHeader("Access-Control-Allow-Origin", "*");//заголовок для разрешения запросов с других доменов
		
		if(((From=="")||(From==null)||(From==" "))&&((To=="")||(To==null)||(To==" "))){//если пустой
			String towrite = FinderWithoutDate();
			towrite = towrite.substring(0, towrite.length()-1);//обрезаем последнюю запятую
			towrite = towrite.replace("x", "},{");//заменяем внутренние символы на скобочки
			
			response.getWriter().println("{\"data\":[{"+towrite+"}]}");
		}
		else if (((From!="")&&(From!=null)&&(From!=" "))&&((To!="")&&(To!=null)&&(To!=" "))){
			
	    	String towrite = FinderWithTwoDates(From,To);
	    	towrite = towrite.substring(0, towrite.length()-1);//обрезаем последнюю запятую
			towrite = towrite.replace("x", "},{");
	    	
	    	response.getWriter().println("{\"data\":[{"+towrite+"}]}");
		
		}
		
		else {
	    	String towrite = FinderWithOneDate(From+To);
	    	towrite = towrite.substring(0, towrite.length()-1);//обрезаем последнюю запятую
			towrite = towrite.replace("x", "},{");
	    	
	    	response.getWriter().println("{\"data\":[{"+towrite+"}]}");
		}
	}
	
	public static String FinderWithTwoDates(String data1, String data2) {
		// надо будет проверить, а реальны ли эти даты
		//но это потом, пока верим
		
		Date today = new Date();
		String toreturn = "";
		SimpleDateFormat format = new SimpleDateFormat("dd.yyyy.MM");
		File file;
		Calendar calendar = new GregorianCalendar();
	    Date askedData1 = today;
	    Date askedData2 = today;
	    try {//если удалось распарсить запрашиваемую дату1
			askedData1 =  format.parse(format.format(Long.parseLong(data1)*1000));
 			try {//если удалось распарсить запрашиваемую дату2
 				askedData2 =  format.parse(format.format(Long.parseLong(data2)*1000));
				long x = askedData2.getTime() - askedData1.getTime();
				if (!(x>0)){return "АХТУНГ! ДАты в неправильном порядке"; }
				else {
					int N=0; // количество целых дней между сегодня и запрашиваемой датой
				    N = (int)Math.ceil((x/86400000));//округление разницы в миллисекундах, деленной на кол-во миллисекунд в дне
				    for (int i=0; i<N+1; i++) {
				    	String fileData  = format.format(askedData1);
				        file = new File("C:" + File.separator + "Users" + File.separator + "Alena" + File.separator + "workspace" + File.separator + "AServlet" + File.separator +"Mosmeteo" + File.separator + fileData + "Mosmeteo" +".txt");
				        toreturn += ReadForVlad(fileData, file);
						calendar.setTime(askedData1); 
						calendar.add(Calendar.DATE, 1); 
					    askedData1 = calendar.getTime(); //Дата 
					}
			    }
			} catch (ParseException e1) {
				System.out.println("Не парсится2");
				e1.printStackTrace();
			}
		} catch (ParseException e1) {
			System.out.println("Не парсится1");
			e1.printStackTrace();
		}
		return toreturn;
	}

	
	public static String FinderWithOneDate(String ask) {
		//System.out.println("Вот такой запрос получили   "+ask);
		Date today = new Date();
		String toreturn = "";
		SimpleDateFormat format = new SimpleDateFormat("dd.yyyy.MM");
		File file;
		Calendar calendar = new GregorianCalendar();
	    Date askedData = today;
	    int N=0; // количество целых дней между сегодня и запрашиваемой датой
		try {//если удалось распарсить запрашиваемую дату
			long ask1 = Long.parseLong(ask);
			askedData = format.parse(format.format(Long.parseLong(ask)*1000));//должна получиться дата на выходе
			
			long x = today.getTime()/1000 - ask1; //askedData.getTime();
			//System.out.println("Получилась разница   "+x);
		    N = (int)Math.ceil((x/86400));
		    System.out.println("Н равно   "+N);
		} catch (ParseException e1) {
			System.out.println("Не парсится");
			e1.printStackTrace();
		}
	    
	    for (int i=0; i<N+1; i++)
	    {
	    	//System.out.println("Заход номер "+i);
	    	String today4 = format.format(askedData);//переводит дату в нужный формат для поиска ее
	        file = new File("C:" + File.separator + "Users" + File.separator + "Alena" + File.separator + "workspace" + File.separator + "AServlet" + File.separator +"Mosmeteo" + File.separator + today4 + "Mosmeteo" +".txt");
	        toreturn += ReadForVlad(today4, file);
	        //System.out.println("Файл  "+file);
	       // System.out.println("Cуществует ли он?  "+file.exists());
	        //if(!file.exists()){
				calendar.setTime(askedData); 
				calendar.add(Calendar.DATE, 1); 
		    	//System.out.println("ДЕнь календаря "+calendar.getTime());
				askedData = calendar.getTime(); //Дата 
			// }
			
	    }
		
	    return toreturn;
		
	   
	}
	
	public static String FinderWithoutDate() {
		Date dateNow = new Date();
	    SimpleDateFormat DayNow = new SimpleDateFormat("dd.yyyy.MM");
	    String today = DayNow.format(dateNow);
	    File file = new File("C:" + File.separator + "Users" + File.separator + "Alena" + File.separator + "workspace" + File.separator + "AServlet" + File.separator +"Mosmeteo" + File.separator + today + "Mosmeteo" +".txt");
		
		return ReadForVlad(today, file);
	}
	
	public static String ReadForVlad(String date, File file) {
		String towrite="";
		BufferedReader in;
		
		try {
			in = new BufferedReader(new FileReader(file));
			try {
				String s;
				int flag = 0;
				int index = 0;
				//В цикле построчно считываем файл
				towrite += "";	
				while ((s = in.readLine()) != null){
					flag++;
					if (flag>2){
						index = s.indexOf("\t\t");
						if (index > 2){
							SimpleDateFormat format = new SimpleDateFormat("HH:mm:ss dd.yyyy.MM");
							try {
								towrite += "\"time\":\""+format.parse(s.substring(0, index-1)+" "+date).getTime()+"\"\n,";
							} catch (ParseException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
								}
							
							s = s.substring(2);
							s = s.substring(index);
							index = s.indexOf("\t\t");
							s = s.substring(2);
							s = s.substring(index);
							
							towrite += "\"temperature\":\""+s.substring(0, index-3)+"\"x";
						 }	
					}
				}
			}
			catch(IOException e) {
		    	logger.log(Level.SEVERE, "Exception reader: ", e);
		    	System.out.println("Ошибка 5 ");
		    	System.out.println(e.toString());
		    }
			finally {
				try {
					in.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		} catch (FileNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		return towrite;
	}


	public static String askRemoteServer(String url, String encoding) throws IOException{
		StringBuffer response = null;
		URL obj = new URL(url);
		HttpURLConnection connection = (HttpURLConnection) obj.openConnection();
		int status =0;
		int connectionCounter = 0;//не больше трех раз
		do {
			if(connectionCounter<3){
				connection = (HttpURLConnection) obj.openConnection();
				connection.setRequestMethod("GET");		
				status = connection.getResponseCode();
			}
		} while (status!= HttpURLConnection.HTTP_OK);//пока не придет положительный ответ от сервера
		if(status== HttpURLConnection.HTTP_OK){//если пришел
			BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream(), encoding));
			String inputLine;
			response = new StringBuffer();
			while ((inputLine = in.readLine()) != null)
			    response.append(inputLine);
			in.close();
		}
		else System.out.println("Сервер не отвечает по адресу "+url);
		
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
	    String towrite = ""; 
	    while (ind2!=-1) {
		    ind2 = answer.indexOf(",");//найити разделитель
			towrite += answer.substring(0, ind2)+ "\t\t"; //вписать в ответ
			answer = answer.substring(ind2+1);//обрезать c разделитель
			ind2 = answer.indexOf(",");
	    } 
	    towrite += answer;
		String file = today+SiteName+".txt";
		String title = "";
		writer(file, towrite, title, SiteName);
	}
	
	public static void ParserCgokiev(String answer) {
		int ind; //индекс начала подстроки
		int ind2; //индекс конца подстроки
		String SiteName="Cgokiev";
		Date dateNow = new Date();
	    SimpleDateFormat DateNow = new SimpleDateFormat("HH:mm:ss ");
	    SimpleDateFormat DayNow = new SimpleDateFormat("dd.yyyy.MM");
	    String today = DayNow.format(dateNow);
	    String Time = DateNow.format(dateNow);
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
	
	public static void ParserMosmeteo(String answer) {
		String SiteName="Mosmeteo";
		int ind2 = 0; //индекс конца подстроки
		int ind1 = 0;
		Date dateNow = new Date();
	    SimpleDateFormat DateNow = new SimpleDateFormat("HH:mm:ss ");
	    SimpleDateFormat DayNow = new SimpleDateFormat("dd.yyyy.MM");
	    String today = DayNow.format(dateNow);
	    String Time = DateNow.format(dateNow);
	    String towrite = "";
	    
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
	
	public static void ParserMosobl(String answer){
		String SiteName="Mosobl";
		int ind2 = 0; //индекс конца подстроки
		int ind1 = 0;
		Date dateNow = new Date();
	    SimpleDateFormat DayNow = new SimpleDateFormat("dd.yyyy.MM");
	    String today = DayNow.format(dateNow);
	    
	    String towrite =  "";
	    String file = today+SiteName+".txt";
		String title = "";
		
	    for (int j=0; j<21; j++){
		    ind2 = answer.indexOf(": left\"");//найити название города 
		    answer = answer.substring(ind2+8);//обрезаем по название
			ind2 = answer.indexOf("</td>");//конец названия
			towrite += answer.substring(0, ind2)+ "\t\t"; //вписать в ответ название
			
			for (int i=0; i<6; i++){
				ind1 = answer.indexOf("acell");//поиск 1 значения
				if (i==2){
					answer = answer.substring(ind1+9);//обрезаем по i-тое значение
					ind1 = answer.indexOf("</b>");//поиск конца значения
					towrite += answer.substring(0, ind1)+ "\t\t"; //вписать в ответ
					answer = answer.substring(ind1);
				}
				else {
					answer = answer.substring(ind1+6);//обрезаем по i-тое значение
					ind1 = answer.indexOf("</td>");//поиск конца значения
					towrite += answer.substring(0, ind1)+ "\t\t"; //вписать в ответ
					answer = answer.substring(ind1);
				}
			}
			writer(file, towrite, title, SiteName);//записываем построчно
			towrite =  "";//обнуляем переменную для записи
	    }	
	}
	
	public static  void writer(String FileName, String str, String title, String SiteName) {
		String T = "время\t \t\tтемпература\tточка росы\t\t\tнаправ. ветра\t\t\tсила ветра\t\t\tатм. давление\t\t\t\t\tвлажность\t";
		String hr = "_______________________________________________________________________________________________________________________________________________________________"; 
		File file = new File(SiteName+ File.separator +FileName);
	    Boolean IsNew = false;
	    try {
	    	if(!file.exists()){//проверяем, что если файл не существует то создаем его
	    		file.createNewFile();
	            System.out.println("Создали файл " + file);
	            logger.log(Level.INFO, "The file was created");
	            IsNew = true;
	        }
	        BufferedWriter out = new BufferedWriter(new FileWriter(file, true));
	        try
	        {
	        	if (IsNew&(SiteName!="Mosobl")) {out.append(T + title); out.newLine(); out.append(hr); out.newLine();}
	            out.append(str);
	            out.newLine();
	        } finally {
	            out.close();
	        }
	    } catch(IOException e) {
	    	logger.log(Level.SEVERE, "Exception with writer!!!!: ", e);
	    	System.out.println("Ошибка 1 ");
	    	System.out.println(e.toString());
	    }
	    

		Date dateNow = new Date();
	    SimpleDateFormat DateNow = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss ");
	    String Time = DateNow.format(dateNow);
	    File file1 = new File(SiteName+ File.separator +"last.txt");
	    try {
	        if(!file1.exists()){//проверяем, что если файл не существует то создаем его
	            file1.createNewFile();
	            System.out.println("Создали файл 1");
	            logger.log(Level.INFO, "The file was created");
	            
	        }
	        BufferedWriter out = new BufferedWriter(new FileWriter(file1, false));
	 
	        try {
	        	out.write(Time);
	        } finally {
	            out.close();
	        }
	    } catch(IOException e) {
	    	logger.log(Level.SEVERE, "Exception with writer!!!!: ", e);
	    	System.out.println("Ошибка 2 ");
	    	System.out.println(e.toString());
	    }
	}
	
	public static String readerLastModification( String SiteName) {
		StringBuilder sb = new StringBuilder();
		File file = new File(SiteName+ File.separator +"last.txt");
		if(!file.exists()){
			try
			{
				boolean created = file.createNewFile();
				if(created){
					System.out.println("!!!!!Файл создан при чтении!!!!");
					logger.log(Level.SEVERE, "There was  no file last.txt: ");
				}
			}
			catch(IOException e){
				logger.log(Level.SEVERE, "Exception with reader: ", e);
				System.out.println("Ошибка чтения");
				System.out.println(e.getMessage());
			}  
		}
		try {
			//Объект для чтения файла в буфер
			BufferedReader in = new BufferedReader(new FileReader(file));
			try {
				//В цикле построчно считываем файл
				String s;
				while ((s = in.readLine()) != null)
					sb.append(s);
			}
			catch(IOException e) {
		    	logger.log(Level.SEVERE, "Exception reader: ", e);
		    	System.out.println("Ошибка 5 ");
		    	System.out.println(e.toString());
		    }
			finally {
				in.close();
			}

		}
		catch(IOException e) {
			logger.log(Level.SEVERE, "Global Exception with reader: ", e);
			System.out.println("Ошибкa 3");
			System.out.println(e.getMessage());
		}
		return sb.toString();
	}

}