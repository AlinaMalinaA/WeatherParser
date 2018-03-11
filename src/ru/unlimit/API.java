package ru.unlimit;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.logging.Level;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Servlet implementation class API
 */
public class API extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public API() {
        super();
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
    //метод отвечает на гет-запрос
    //из запроса извлекаются параметры 
    //вызывается функция поиска файлов с нужным числом параметров
    //затем то, что вернет функция, оборачиваем в символы до JSON
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException  {
		String From;
		
		if (request.getParameter("fromdata")==null) {System.out.println("Проверка "); }
		else {
			try {
				From = java.net.URLDecoder.decode(request.getParameter("fromdata"), "UTF-8");
				String To = java.net.URLDecoder.decode(request.getParameter("todata"), "UTF-8");//дата до которой считать
				System.out.println("Запрос целиком "+request);
				System.out.println("Запрос был "+From+" и "+To);
				Weather.logg("Запрос был "+From+" и "+To, "doGet", "info");
				
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
					
			    	String towrite = FinderWithTwoDates(From,To);//если были посланы две даты
			    	towrite = towrite.substring(0, towrite.length()-1);//обрезаем последнюю запятую
					towrite = towrite.replace("x", "},{");
			    	
			    	response.getWriter().println("{\"data\":[{"+towrite+"}]}");
				
				}
				
				else {
			    	String towrite = FinderWithOneDate(From+To);//если была послана только одна дата
			    	towrite = towrite.substring(0, towrite.length()-1);//обрезаем последнюю запятую
					towrite = towrite.replace("x", "},{");
			    	
			    	response.getWriter().println("{\"data\":[{"+towrite+"}]}");
				}
			} catch (NullPointerException e) {
				Weather.logg("поймана ошибка  "+e +" ", "doGet, API", "exception");
				System.out.println("поймана ошибка 1 "+e +" ");
				response.getWriter().println("null");
			} catch (FileNotFoundException e) {
				Weather.logg("поймана ошибка  "+e +" ", "doGet, API", "exception");
				System.out.println("поймана ошибка 2 "+e +" ");
				response.getWriter().println("null");
			} catch (StringIndexOutOfBoundsException e) {
				Weather.logg("поймана ошибка  "+e +" ", "doGet, API", "exception");
				System.out.println("поймана ошибка 3 "+e +" ");
				response.getWriter().println("null");
			}catch (NumberFormatException e) {
				Weather.logg("поймана ошибка  "+e +" ", "doGet, API", "exception");
				System.out.println("поймана ошибка 4 "+e +" ");
				response.getWriter().println("null");
			} catch (IOException e) {
				Weather.logg("поймана ошибка  "+e +" ", "doGet, API", "exception");
				System.out.println("поймана ошибка 5 "+e +" ");
				response.getWriter().println("null");
			}
		}
	}
	
    //метод ищет файлы записанные между первой и второй датой
    //парсит эти даты в человековаримый вид
    //вызывает функцию ReadForVlad
    //возвращает то,что вернет эта функция
	public static String FinderWithTwoDates(String data1, String data2) {
		// надо будет проверить, а реальны ли эти даты
		//но это потом, пока верим
		
		
		System.out.println("Даты "+data1+" и "+data2);
		Date today = new Date();
		String toreturn = "";
		SimpleDateFormat format = new SimpleDateFormat("dd.yyyy.MM");
		File file;
		Calendar calendar = new GregorianCalendar();
	    Date askedData1 = today;
	    Date askedData2 = today;
	    try {//добавить проверку на сенуществующие даты
			askedData1 =  format.parse(format.format(Long.parseLong(data1)*1000));
			//System.out.println("Пропарсилось 1 "+askedData1);
 			try {//если удалось распарсить запрашиваемую дату2
 				askedData2 =  format.parse(format.format(Long.parseLong(data2)*1000));
 				//System.out.println("Пропарсилось 2 "+askedData2);
				long x = askedData2.getTime() - askedData1.getTime();
				if (!(x>0)){ return "null"; }//даты в неправильном порядке
				else {
					int N=0; // количество целых дней между сегодня и запрашиваемой датой
				    N = (int)Math.ceil((x/86400000));//округление разницы в миллисекундах, деленной на кол-во миллисекунд в дне
				    
				    for (int i=0; i<N+1; i++) {
				    	String fileData  = format.format(askedData1);//дата этого файла
				        file = new File("C:" + File.separator + "Users" + File.separator + "Alena" + File.separator + "workspace" + File.separator + "AServlet" + File.separator +"Mosmeteo" + File.separator + fileData + "Mosmeteo" +".txt");
				        toreturn += ReadForVlad(fileData, file);
						calendar.setTime(askedData1); 
						calendar.add(Calendar.DATE, 1); 
					    askedData1 = calendar.getTime(); //Дата 
					}
			    }
			} catch (ParseException e1) {
				Weather.logg("Не парсится вторая дата "+e1 +" ", "FinderWithTwoDates, API", "exception");
				e1.printStackTrace();
			}
		} catch (ParseException e) {
			Weather.logg("Не парсится первая дата "+e +" ", "doGFinderWithTwoDateset, API", "exception");
		} catch (NumberFormatException e) {
			Weather.logg("Не парсится первая дата "+e +" ", "doGFinderWithTwoDateset, API", "exception");
		} catch (StringIndexOutOfBoundsException e) {
			Weather.logg("Проблемы с парсингом запроса "+e +" ", "doGFinderWithTwoDateset, API", "exception");
		}
	    //System.out.println("Возвращаем "+toreturn);
		return toreturn;
	}
	
	
	//метод ищет файлы между сегодняшней датой и запрошенной
	//парсит запрошенную дату
	//проходит рекурсивно по дням от запрошенной даты до сегодня
	//вызывает метод ReadForVlad и возвращает то, что он вернет
	public static String FinderWithOneDate(String ask) {
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
			
			long x = today.getTime()/1000 - ask1;
		    N = (int)Math.ceil((x/86400));
		} catch (ParseException e1) {
			Weather.logg("Не парсится"+e1 +" ", "FinderWithOneDate, API", "exception");
		}
	    
	    for (int i=0; i<N+1; i++)
	    {
	    	String today4 = format.format(askedData);//переводит дату в нужный формат для поиска ее
	        file = new File("C:" + File.separator + "Users" + File.separator + "Alena" + File.separator + "workspace" + File.separator + "AServlet" + File.separator +"Mosmeteo" + File.separator + today4 + "Mosmeteo" +".txt");
	        toreturn += ReadForVlad(today4, file);
			calendar.setTime(askedData); 
			calendar.add(Calendar.DATE, 1); 
			askedData = calendar.getTime(); //Дата 	
	    }
	    return toreturn;
	}
	
	
	//метод ищет сегодняшний файл и возвращает то, что вернет ReadForVlad
	public static String FinderWithoutDate() {
		Date dateNow = new Date();
	    SimpleDateFormat DayNow = new SimpleDateFormat("dd.yyyy.MM");
	    String today = DayNow.format(dateNow);
	    File file = new File("C:" + File.separator + "Users" + File.separator + "Alena" + File.separator + "workspace" + File.separator + "AServlet" + File.separator +"Mosmeteo" + File.separator + today + "Mosmeteo" +".txt");
		
		return ReadForVlad(today, file);
	}
	
	
	//метод читает данные из файла и парсит их по JSON
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
								Weather.logg("Ошибка парсинга "+e.toString()+" ", "ReadForVlad, API", "exception");
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
			catch(StringIndexOutOfBoundsException e) {
				Weather.logg("Ошибка  "+e.toString()+" ", "ReadForVlad, API", "exception");
		    }
			finally {
				try {
					in.close();
				} catch (IOException e) {
					Weather.logg("Общая ошибка после закрытия потока  "+e.toString()+" ", "ReadForVlad, API", "exception");
				}
			}
		}
		catch(FileNotFoundException e) {
			Weather.logg("Не найден файл "+e +" ", "ReadForVlad, API", "exception");
		   	checkAllFiles();
		   }
		catch(IOException e1) {
			Weather.logg("Общая ошибка  "+e1.toString() +" ", "ReadForVlad, API", "exception");
		}
		return towrite;
	}
    
	//на каждыц день от начала летописи до сегодня
	//начало летописи - 1.10.2017
	//проверить существование файла для каждого сайта
	//если файла не существует - создать пустой
	//или не пустой, но с пустыми значениями
    //метод проверяет пропущенные файлы и создает пустые на их месте
	private static void checkAllFiles() {
		//дата сейчас
		Date dateNow = new Date();
		SimpleDateFormat format = new SimpleDateFormat("dd.yyyy.MM");
		String today = format.format(dateNow);
		Calendar calendar = new GregorianCalendar();
		
		String begin = "01.2017.10";//начало летоисчисления для этого проекта
		
		File file;
		String [] sites = { "Meteoinfo", "Mosmeteo", "Mosobl", "Cgokiev", "Meteod"};
		do {
			for (int i = 0; i < sites.length; i++){//на каждый сайт - проверка
				file = new File("C:" + File.separator + "Users" + File.separator + "Alena" + File.separator + "workspace" + File.separator + "AServlet" + File.separator +sites[i] + File.separator + today + sites[i] +".txt");
				if(!file.exists()){//если не существует
					
					boolean created;
					try {
						created = file.createNewFile();
						if(created){
							Weather.logg("!!!!!Файл создан для "+sites[i] + " от "+today, "checkAllFiles, API", "warning");
							BufferedWriter writer = new BufferedWriter(new FileWriter(file));
							writer.write("null");
						}
					} catch (IOException e) {
						Weather.logg("Общая ошибка "+e, "checkAllFiles, API", "exception");
					}
				}
			}
			calendar.add(Calendar.DAY_OF_MONTH, -1);
			today = format.format(calendar.getTime()); //Дата предыдущая
		}
		while(!today.equals(begin));
	}

	
	//отдает запрос в метод гет
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		doGet(request, response);
	}

}
