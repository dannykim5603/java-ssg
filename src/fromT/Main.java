//조장 메세지	앙 기모띠~
// 앙 동연띠~!@~!@~!@!~@!~@~!@~!@~!@!~@
package fromT; // 작업한거

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

class Main {
	public static void main(String[] args) {
		App app = new App();
		app.start();
	}
}

// Session
// 현재 사용자가 이용중인 정보
// 이 안의 정보는 사용자가 프로그램을 사용할 때 동안은 계속 유지된다.
class Session {
	private Member loginedMember;
	private Board currentBoard;

	public Member getLoginedMember() {
		return loginedMember;
	}

	public void setLoginedMember(Member loginedMember) {
		this.loginedMember = loginedMember;
	}

	public Board getCurrentBoard() {
		return currentBoard;
	}

	public void setCurrentBoard(Board currentBoard) {
		this.currentBoard = currentBoard;
	}

	public boolean isLogined() {
		return loginedMember != null;
	}
}

// Factory
// 프로그램 전체에서 공유되는 객체 리모콘을 보관하는 클래스

class Factory {
	private static Session session;
	private static DB db;
	private static BuildService buildService;
	private static ArticleService articleService;
	private static ArticleDao articleDao;
	private static MemberService memberService;
	private static MemberDao memberDao;
	private static Scanner scanner;

	public static Session getSession() {
		if (session == null) {
			session = new Session();
		}

		return session;
	}

	public static Scanner getScanner() {
		if (scanner == null) {
			scanner = new Scanner(System.in);
		}

		return scanner;
	}

	public static DB getDB() {
		if (db == null) {
			db = new DB();
		}

		return db;
	}

	public static ArticleService getArticleService() {
		if (articleService == null) {
			articleService = new ArticleService();
		}

		return articleService;
	}

	public static ArticleDao getArticleDao() {
		if (articleDao == null) {
			articleDao = new ArticleDao();
		}

		return articleDao;
	}

	public static MemberService getMemberService() {
		if (memberService == null) {
			memberService = new MemberService();
		}
		return memberService;
	}

	public static MemberDao getMemberDao() {
		if (memberDao == null) {
			memberDao = new MemberDao();
		}

		return memberDao;
	}

	public static BuildService getBuildService() {
		if (buildService == null) {
			buildService = new BuildService();
		}

		return buildService;
	}
}

// App
class App {
	private Map<String, Controller> controllers;

// 컨트롤러 만들고 한곳에 정리
// 나중에 컨트롤러 이름으로 쉽게 찾아쓸 수 있게 하려고 Map 사용
	void initControllers() {
		controllers = new HashMap<>();
		controllers.put("build", new BuildController());
		controllers.put("article", new ArticleController());
		controllers.put("member", new MemberController());
	}

	public App() {
// 컨트롤러 등록
		initControllers();

// 관리자 회원 생성
		Factory.getMemberService().join("admin", "admin", "관리자");
// 공지사항 게시판 생성
		Factory.getArticleService().makeBoard("공지시항", "notice");
// 자유 게시판 생성
		Factory.getArticleService().makeBoard("자유게시판", "free");
// 현재 게시판을 1번 게시판으로 선택
		Factory.getSession().setCurrentBoard(Factory.getArticleService().getBoard(1));
// 임시 : 현재 로그인 된 회원은 1번 회원으로 지정, 이건 나중에 회원가입, 로그인 추가되면 제거해야함
		Factory.getSession().setLoginedMember(Factory.getMemberService().getMember(1));
	}

	public void start() {

		while (true) {
			System.out.printf("명령어 : ");
			String command = Factory.getScanner().nextLine().trim();

			if (command.length() == 0) {
				continue;
			} else if (command.equals("exit")) {
				break;
			}

			Request request = new Request(command);

			if (request.isValidRequest() == false) {// request가 null(false) 일때 continue
				continue;
			}

			if (controllers.containsKey(request.getControllerName()) == false) {
				continue;
			}

			controllers.get(request.getControllerName()).doAction(request);
		}

		Factory.getScanner().close();
	}
}

// Request
class Request {
	private String requestStr;
	private String controllerName;
	private String actionName;
	private String arg1;
	private String arg2;
	private String arg3;

	boolean isValidRequest() {
		return actionName != null;
	}

	Request(String requestStr) {
		this.requestStr = requestStr;
		String[] requestStrBits = requestStr.split(" ");
		this.controllerName = requestStrBits[0];

		if (requestStrBits.length > 1) {
			this.actionName = requestStrBits[1];
		}

		if (requestStrBits.length > 2) {
			this.arg1 = requestStrBits[2];
		}

		if (requestStrBits.length > 3) {
			this.arg2 = requestStrBits[3];
		}

		if (requestStrBits.length > 4) {
			this.arg3 = requestStrBits[4];
		}
	}

	public String getControllerName() {
		return controllerName;
	}

	public void setControllerName(String controllerName) {
		this.controllerName = controllerName;
	}

	public String getActionName() {
		return actionName;
	}

	public void setActionName(String actionName) {
		this.actionName = actionName;
	}

	public String getArg1() {
		return arg1;
	}

	public void setArg1(String arg1) {
		this.arg1 = arg1;
	}

	public String getArg2() {
		return arg2;
	}

	public void setArg2(String arg2) {
		this.arg2 = arg2;
	}

	public String getArg3() {
		return arg3;
	}

	public void setArg3(String arg3) {
		this.arg3 = arg3;
	}
}

// Controller
abstract class Controller {
	abstract void doAction(Request request);
}

class ArticleController extends Controller {
	private ArticleService articleService;

	ArticleController() {
		articleService = Factory.getArticleService();
	}

	public void doAction(Request request) {
		if (request.getActionName().equals("list")) {
			if (request.getArg1() == null) {
				System.out.println("리스트 페이지 번호를 입력해 주세요.");
			} else if(request.getArg1() != null){
				int num = Integer.parseInt(request.getArg1());
				actionList(num);
			} else if (request.getArg1() != null && request.getArg2() != null) {
				int num = Integer.parseInt(request.getArg1());
				String keyword = request.getArg2();
				actionList(num , keyword);
			}
		} else if (request.getActionName().equals("write")) {
			actionWrite(request);
		} else if (request.getActionName().equals("modify")) {
			if (request.getArg1() == null) {
				System.out.println("게시물 번호를 입력해 주세요.");
			} else {
				int num = Integer.parseInt(request.getArg1());
				actionModify(num);
			}
		} else if (request.getActionName().equals("detail")) {
			if (request.getArg1() == null) {
				System.out.println("게시물 번호를 입력해 주세요.");
			} else {
				int num = Integer.parseInt(request.getArg1());
				actionDetail(num);
			}
		} else if (request.getActionName().equals("delete")) {
			if (request.getArg1() == null) {
				System.out.println("게시물 번호를 입력해 주세요.");
			} else {
				int num = Integer.parseInt(request.getArg1());
				actionDelete(num);
			}
		} else if (request.getActionName().equals("makeboard")) {
			actionMakeBoard(request);
		} else if (request.getActionName().equals("listboard")) {
			actionlistBoard(request);
		} else if (request.getActionName().equals("deleteboard")) {
			String code = request.getArg1();
			actionDeleteBoard(code);
		} else if (request.getActionName().equals("changeboard")) {
			if (request.getArg1() == null) {
				System.out.println("게시판 번호를 입력해 주세요.");
			} else {
				int num = Integer.parseInt(request.getArg1());
				actionChangeBoard(num);
			}
		}
	}

	

	private void actionChangeBoard(int num) {

		int id = Factory.getSession().getCurrentBoard().getId();
		if (id != Factory.getArticleService().getBoard(num).getId()) {
			Factory.getSession().setCurrentBoard(Factory.getArticleService().getBoard(num));
			Board board = Factory.getSession().getCurrentBoard();
			System.out.printf("%s (으)로 변경되었습니다.%n", board.getName());
		} else {
			System.out.println("현재 사용중인 게시판 입니다.");
		} // 게시판 중복메시지 안뜸
	}

	private void actionDeleteBoard(String code) {
		articleService.deleteBoardByCode(code);
	}

	private void actionlistBoard(Request request) {
		List<Board> boards = articleService.getBoards();

		System.out.println(boards);
	}

	private void actionMakeBoard(Request request) {
		List<Board> boards = articleService.getBoards();
		String boardName;
		String boardCode;

		while (true) {
			System.out.printf("생성하실 게시판 이름을 입력해 주세요:");
			boardName = Factory.getScanner().nextLine();
			System.out.printf("생성하실 게시판 코드를 입력해 주세요:");
			boardCode = Factory.getScanner().nextLine();
			if (articleService.makeBoard(boardName, boardCode) == -1) {
				System.out.println("이미 사용중인 코드입니다.");
				continue;
			} else {
				break;
			}
		}
		articleService.makeBoard(boardName, boardCode);
	}

	private void actionDelete(int num) {
		articleService.deleteArticleById(num);
	}

	private void actionDetail(int num) {
		System.out.println(articleService.getArticlebyId(num));
	}

	private void actionModify(int num) {
		String title;
		String body;
		if (Factory.getSession().isLogined() == true) {
			Article article = Factory.getArticleService().getArticlebyId(num);
			System.out.printf("제목 : ");
			title = Factory.getScanner().nextLine();
			System.out.printf("내용 : ");
			body = Factory.getScanner().nextLine();
			articleService.modify(num, title, body);
		}
	}
	
	private void actionList(int num, String keyword) {
		String code = Factory.getSession().getCurrentBoard().getCode();
		List<Article> articles = articleService.getArticlesByBoardCode(code);
		int end = num * 5;
		int start = end - 5;
		
		for (int i = start; i < end; i++) {
			if ( i >= articles.size()) {
				break;
			}else if(articles.indexOf(keyword) != 0) {
				System.out.println(articles.get(i));
			}
		}
	}		

	private void actionList(int num) {
		String code = Factory.getSession().getCurrentBoard().getCode();
		List<Article> articles = articleService.getArticlesByBoardCode(code);
		int end = num * 5;
		int start = end - 5;
		
		for (int i = start; i < end; i++) {
			if ( i >= articles.size()) {
				break;
			}else {
				System.out.println(articles.get(i));
			}
		}
	}

	private void actionWrite(Request request) {
		if (Factory.getSession().isLogined() == true) {
			System.out.printf("제목 : ");
			String title = Factory.getScanner().nextLine();
			System.out.printf("내용 : ");
			String body = Factory.getScanner().nextLine();

// 현재 게시판 id 가져오기
			int boardId = Factory.getSession().getCurrentBoard().getId();

// 현재 로그인한 회원의 id 가져오기
			int memberId = Factory.getSession().getLoginedMember().getId();
			int newId = articleService.write(boardId, memberId, title, body);

			System.out.printf("%d번 글이 생성되었습니다.\n", newId);
		} else {
			System.out.println("로그인이 필요한 서비스 입니다.");
		}
	}
}

class BuildController extends Controller {
	private BuildService buildService;

	BuildController() {
		buildService = Factory.getBuildService();
	}

	@Override
	void doAction(Request request) {
		if (request.getActionName().equals("site")) {
			actionSite(request);
		}
	}

	private void actionSite(Request request) {
		buildService.buildSite();
		System.out.println("생성이 완료 되었습니다.");
	}
}

class MemberController extends Controller {
	private MemberService memberService;

	MemberController() {
		memberService = Factory.getMemberService();
	}

	void doAction(Request request) {
		if (request.getActionName().equals("logout")) {
			actionLogout(request);
		} else if (request.getActionName().equals("login")) {
			actionLogin(request);
		} else if (request.getActionName().equals("whoami")) {
			actionWhoami(request);
		} else if (request.getActionName().equals("join")) {
			actionJoin(request);
		}
	}

	private void actionJoin(Request request) {
		String loginId;
		String loginPw;
		String name;

		if (Factory.getSession().isLogined() == false) {
			while (true) {
				System.out.printf("사용하실 아이디를 입력해 주십시오.%n");
				System.out.printf(" >");
				loginId = Factory.getScanner().next().trim();
				if (loginId.length() == 0) {
					System.out.println("아이디를 입력해 주십시요.");
					continue;
				}
				if (Factory.getDB().getMemberByLoginId(loginId) != null) {
					System.out.println("이미 존재하는 아이디 입니다.");
					continue;
				}
				break;
			}
			while (true) {
				System.out.printf("사용하실 비밀번호를 입력해 주십시오.%n");
				System.out.printf(" >");
				loginPw = Factory.getScanner().next().trim();
				if (loginPw.length() == 0) {
					System.out.println("비밀번호를 입력해 주십시요.");
					continue;
				}
				if (loginPw.length() < 4) {
					System.out.println("비밀번호는 4자 이상이어야 합니다.");
					continue;
				}
				break;
			}
			System.out.printf("사용하실 이름을 입력해 주십시오.%n");
			System.out.printf(" >");
			name = Factory.getScanner().nextLine().trim();

			memberService.join(loginId, loginPw, name);
		} else {
			System.out.println("로그아웃 후 이용하실 수 있는 서비스 입니다.");
		}
	}

	private void actionWhoami(Request request) {
		Member loginedMember = Factory.getSession().getLoginedMember();

		if (loginedMember == null) {
			System.out.println("나그네");
		} else {
			System.out.println(loginedMember.getName());
		}
	}

	private void actionLogin(Request request) {
		if (Factory.getSession().isLogined() == false) {
			System.out.printf("로그인 아이디 : ");
			String loginId = Factory.getScanner().nextLine().trim();

			System.out.printf("로그인 비번 : ");
			String loginPw = Factory.getScanner().nextLine().trim();

			Member member = memberService.getMemberByLoginIdAndLoginPw(loginId, loginPw);

			if (member == null) {
				System.out.println("일치하는 회원이 없습니다.");
			} else {
				System.out.println(member.getName() + "님 환영합니다.");
				Factory.getSession().setLoginedMember(member);
			}
		} else {
			System.out.println("이미 로그인이 되어 있습니다.");
		}
	}

	private void actionLogout(Request request) {
		Member loginedMember = Factory.getSession().getLoginedMember();

		if (loginedMember != null) {
			Session session = Factory.getSession();
			System.out.println("로그아웃 되었습니다.");
			session.setLoginedMember(null);
		}

	}
}

// Service
class BuildService {
	ArticleService articleService;

	BuildService() {
		articleService = Factory.getArticleService();
	}
//	public void buildIndex() {
//		Util.makeDir("site_template/home");
//		
//		String head = Util.getFileContents("site_template/part/head.html");
//		String foot = Util.getFileContents("site_template/part/foot.html");
//		
//		String html = head + foot;
//		
//		Util.writeFileContents("site_template/index.html" ,html);
//	}

	public void buildSite() {
		Util.makeDir("site");
		Util.makeDir("site/article");
		Util.makeDir("site_template/resource");
		Util.makeDir("site_template/home");
		Util.makeDir("site_template/stat");
		Util.makeDir("site_template/part");
		Util.makeDir("site_template/article");
		
		
		String head = Util.getFileContents("site_template/part/head.html");
		String foot = Util.getFileContents("site_template/part/foot.html");

// 각 게시판 별 게시물리스트 페이지 생성
		List<Board> boards = articleService.getBoards();

		for (Board board : boards) {
			String fileName = board.getCode() + "-list-1.html";

			String html = "";

			List<Article> articles = articleService.getArticlesByBoardCode(board.getCode());

			String template = Util.getFileContents("site_template/article/list.html");

			for (Article article : articles) {
				html += "<tr>";
				html += "<td>" + article.getId() + "</td>";
				html += "<td>" + article.getRegDate() + "</td>";
				html += "<td><a href=\"" + article.getId() + ".html\">";
				html += "<td>" + article.getTitle() + "</a></td>";
				html += "</tr>";
			}

			html = template.replace("${TR}", html);

			html = head + html + foot;

			Util.writeFileContents("site/article/" + fileName, html);
		}

// 게시물 별 파일 생성
		List<Article> articles = articleService.getArticles();

		for (Article article : articles) {
			String html = "";

			html += "<div>제목 : " + article.getTitle() + "</div>";
			html += "<div>내용 : " + article.getBody() + "</div>";
			html += "<div><a href=\"" + (article.getId() - 1) + ".html\">이전글</a></div>";
			html += "<div><a href=\"" + (article.getId() + 1) + ".html\">다음글</a></div>";
			
			html = head + html + foot;

			Util.writeFileContents("site/article/" + article.getId() + ".html", html);
		}
	}

}

class ArticleService {
	private ArticleDao articleDao;

	ArticleService() {
		articleDao = Factory.getArticleDao();
	}

	public List<Article> getArticlesByBoardCode(String code) {
		return articleDao.getArticlesByBoardCode(code);
	}

	public Article getArticlesFromCurrentBoard(Board currentBoard) {
		return articleDao.getArticlesFromCurrentBoard(currentBoard);
	}

	public List<Article> getArticlesFromCurrentBoard() {
		return articleDao.getArticles();
	}

	public void deleteBoardByCode(String code) {
		articleDao.deleteBoard(code);
	}

	public List<Board> getBoards() {
		return articleDao.getBoards();
	}

	public void modify(int num, String title, String body) {
		articleDao.modifyArticleById(num, title, body);
	}

	public void deleteArticleById(int num) {
		articleDao.deleteArticleById(num);
	}

	public Article getArticlebyId(int id) {
		return articleDao.getArticlebyId(id);
	}

	public int makeBoard(String name, String code) {
		Board oldBoard = articleDao.getBoardByCode(code);

		if (oldBoard != null) {
			return -1;
		}

		Board board = new Board(name, code);
		return articleDao.saveBoard(board);
	}

	public Board getBoard(int id) {
		return articleDao.getBoard(id);
	}

	public int write(int boardId, int memberId, String title, String body) {
		Article article = new Article(boardId, memberId, title, body);
		return articleDao.save(article);
	}

	public List<Article> getArticles() {
		return articleDao.getArticles();
	}

}

class MemberService {
	private MemberDao memberDao;

	MemberService() {
		memberDao = Factory.getMemberDao();
	}

	public Member getMemberByLoginIdAndLoginPw(String loginId, String loginPw) {
		return memberDao.getMemberByLoginIdAndLoginPw(loginId, loginPw);
	}

	public int join(String loginId, String loginPw, String name) {
		Member oldMember = memberDao.getMemberByLoginId(loginId);

		if (oldMember != null) {
			return -1;
		}

		Member member = new Member(loginId, loginPw, name);
		return memberDao.save(member);
	}

	public Member getMember(int id) {
		return memberDao.getMember(id);
	}
}

// Dao
class ArticleDao {
	DB db;

	ArticleDao() {
		db = Factory.getDB();
	}

	public List<Article> getArticlesByBoardCode(String code) {
		return db.getArticlesByBoardCode(code);
	}

	public Article getArticlesFromCurrentBoard(Board currentBoard) {
		return db.getArticlesFromCurrentBoard(currentBoard);
	}

	public void deleteBoard(String code) {
		Board board = getBoardByCode(code);
		if (board == null) {
			System.out.println("존재하지 않는 게시판 입니다.");
		}
		db.deleteBoard(board);
	}

	public List<Board> getBoards() {
		return db.getBoards();
	}

	public void modifyArticleById(int num, String title, String body) {
		Article article = getArticlebyId(num);
		db.modifyArticleById(article, title, body);
	}

	public void deleteArticleById(int num) {
		Article article = getArticlebyId(num);

		db.deleteArticle(article);
	}

	public Article getArticlebyId(int id) {
		return db.getArticlebyId(id);
	}

	public Board getBoardByCode(String code) {
		return db.getBoardByCode(code);
	}

	public int saveBoard(Board board) {
		return db.saveBoard(board);
	}

	public int save(Article article) {
		return db.saveArticle(article);
	}

	public Board getBoard(int id) {
		return db.getBoard(id);
	}

	public List<Article> getArticles() {
		return db.getArticles();
	}

}

class MemberDao {
	DB db;

	MemberDao() {
		db = Factory.getDB();
	}

	public Member getMemberByLoginIdAndLoginPw(String loginId, String loginPw) {
		return db.getMemberByLoginIdAndLoginPw(loginId, loginPw);
	}

	public Member getMemberByLoginId(String loginId) {
		return db.getMemberByLoginId(loginId);
	}

	public Member getMember(int id) {
		return db.getMember(id);
	}

	public int save(Member member) {
		return db.saveMember(member);
	}
}

// DB
class DB {
	private Map<String, Table> tables;

	public DB() {
		String dbDirPath = getDirPath();
		Util.makeDir(dbDirPath);

		tables = new HashMap<>();

		tables.put("article", new Table<Article>(Article.class, dbDirPath));
		tables.put("board", new Table<Board>(Board.class, dbDirPath));
		tables.put("member", new Table<Member>(Member.class, dbDirPath));
	}

	public List<Article> getArticlesByBoardCode(String code) {
		Board board = getBoardByCode(code);
		// free > 2
		// notice > 1

		List<Article> articles = getArticles();
		List<Article> newArticles = new ArrayList<>();

		for (Article article : articles) {
			if (article.getBoardId() == board.getId()) {
				newArticles.add(article);
			}
		}
		return newArticles;
	}

	public Article getArticlesFromCurrentBoard(Board currentBoard) {
		List<Article> articles = getArticles();
		for (Article article : articles) {
			if (article.getBoardId() == currentBoard.getId()) {
				return article;
			}
		}

		return null;
	}

	public Article getArticlebyId(int id) {
		List<Article> articles = getArticles();

		for (Article article : articles) {
			if (article.getId() == id) {
				return article;
			}
		}

		return null;
	}

	public void deleteBoard(Board board) {
		tables.get("board").deleteRow(board);
	}

	public void modifyArticleById(Article article, String title, String body) {
		tables.get("article").modify(article, title, body);
	}

	public void deleteArticle(Article article) {
		tables.get("article").deleteRow(article);
	}

	public Member getMemberByLoginIdAndLoginPw(String loginId, String loginPw) {
		List<Member> members = getMembers();

		for (Member member : members) {
			if (member.getLoginId().equals(loginId) && member.getLoginPw().equals(loginPw)) {
				return member;
			}
		}

		return null;
	}

	public Member getMemberByLoginId(String loginId) {
		List<Member> members = getMembers();

		for (Member member : members) {
			if (member.getLoginId().equals(loginId)) {
				return member;
			}
		}
		return null;
	}

	public List<Member> getMembers() {
		return tables.get("member").getRows();
	}

	public Board getBoardByCode(String code) {
		List<Board> boards = getBoards();

		for (Board board : boards) {
			if (board.getCode().equals(code)) {
				return board;
			}
		}

		return null;
	}

	public List<Board> getBoards() {
		return tables.get("board").getRows();
	}

	public Member getMember(int id) {
		return (Member) tables.get("member").getRow(id);
	}

	public int saveBoard(Board board) {
		return tables.get("board").saveRow(board);
	}

	public String getDirPath() {
		return "db";
	}

	public int saveMember(Member member) {
		return tables.get("member").saveRow(member);
	}

	public Board getBoard(int id) {
		return (Board) tables.get("board").getRow(id);
	}

	public List<Article> getArticles() {
		tables.get("article").getRows();
		return tables.get("article").getRows();
	}

	public int saveArticle(Article article) {
		return tables.get("article").saveRow(article);
	}

	public void backup() {
		for (String tableName : tables.keySet()) {
			Table table = tables.get(tableName);
			table.backup();
		}
	}
}

// Table
class Table<T> {
	private Class<T> dataCls;
	private String tableName;
	private String tableDirPath;

	public Table(Class<T> dataCls, String dbDirPath) {
		this.dataCls = dataCls;
		this.tableName = Util.lcfirst(dataCls.getCanonicalName());
		this.tableDirPath = dbDirPath + "/" + this.tableName;

		Util.makeDir(tableDirPath);
	}

	public void modify(Article article, String title, String body) {
		Dto dto = (Dto) article;
		T data = (T) article;

		String FilePath = getRowFilePath(dto.getId());
		File file = new File(FilePath);

		Util.deleteFile(FilePath);

		article.setTitle(title);
		article.setBody(body);
		data = (T) article;

		String rowFilePath = getRowFilePath(dto.getId());

		Util.writeJsonFile(rowFilePath, data);
	}

	public void deleteRow(T data) {
		Dto dto = (Dto) data;

		String FilePath = getRowFilePath(dto.getId());
		File file = new File(FilePath);

		Util.deleteFile(FilePath);
		System.out.println("삭제되었습니다.");

	}

	public Article getRows(int id) {
		int lastId = getLastId();

		Article rows = new Article();

		for (int i = 1; i <= lastId; i++) {
			Article row = (Article) getRow(id);

			if (row.getMemberId() == i) {

			}
		}

		return rows;
	}

	private String getTableName() {
		return tableName;
	}

	public int saveRow(T data) {
		Dto dto = (Dto) data;

		if (dto.getId() == 0) {
			int lastId = getLastId();
			int newId = lastId + 1;
			dto.setId(newId);
			setLastId(newId);
		}

		String rowFilePath = getRowFilePath(dto.getId());

		Util.writeJsonFile(rowFilePath, data);

		return dto.getId();
	};

	private String getRowFilePath(int id) {
		return tableDirPath + "/" + id + ".json";
	}

	private void setLastId(int lastId) {
		String filePath = getLastIdFilePath();
		Util.writeFileContents(filePath, lastId);
	}

	private int getLastId() {
		String filePath = getLastIdFilePath();

		if (Util.isFileExists(filePath) == false) {
			int lastId = 0;
			Util.writeFileContents(filePath, lastId);
			return lastId;
		}

		return Integer.parseInt(Util.getFileContents(filePath));
	}

	private String getLastIdFilePath() {
		return this.tableDirPath + "/lastId.txt";
	}

	public T getRow(int id) {
		return (T) Util.getObjectFromJson(getRowFilePath(id), dataCls);
	}

	public void backup() {

	}

	void delete(int id) {
		/* 구현 */
	};

	List<T> getRows() {
		int lastId = getLastId();

		List<T> rows = new ArrayList<>();

		for (int id = 1; id <= lastId; id++) {
			T row = getRow(id);

			if (row != null) {
				rows.add(row);
			}
		}

		return rows;
	};
}

// DTO
abstract class Dto {
	private int id;
	private String regDate;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getRegDate() {
		return regDate;
	}

	public void setRegDate(String regDate) {
		this.regDate = regDate;
	}

	Dto() {
		this(0);
	}

	Dto(int id) {
		this(id, Util.getNowDateStr());
	}

	Dto(int id, String regDate) {
		this.id = id;
		this.regDate = regDate;
	}
}

class Board extends Dto {
	private String name;
	private String code;

	@Override
	public String toString() {
		return String.format("%n번호 : %s%n 이름 : %s%n 코드 : %s%n", getId(), name, code);
	}

	public Board() {
	}

	public Board(String name, String code) {
		this.name = name;
		this.code = code;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

}

class Article extends Dto {
	private int boardId;
	private int memberId;
	private String title;
	private String body;

	public Article() {

	}

	@Override
	public String toString() {
		return String.format("%n게시판 번호 : %s 회원 아이디 : %s%n게시 번호 : %s 게시 날짜 : %s%n제목 : %s%n내용 : %s%n%n ", boardId,
				memberId, getId(), getRegDate(), title, body);
	}

	public Article(int boardId, int memberId, String title, String body) {
		this.boardId = boardId;
		this.memberId = memberId;
		this.title = title;
		this.body = body;
	}

	public int getBoardId() {
		return boardId;
	}

	public void setBoardId(int boardId) {
		this.boardId = boardId;
	}

	public int getMemberId() {
		return memberId;
	}

	public void setMemberId(int memberId) {
		this.memberId = memberId;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getBody() {
		return body;
	}

	public void setBody(String body) {
		this.body = body;
	}
}

class ArticleReply extends Dto {
	private int id;
	private String regDate;
	private int articleId;
	private int memberId;
	private String body;

	ArticleReply() {

	}

	public int getArticleId() {
		return articleId;
	}

	public void setArticleId(int articleId) {
		this.articleId = articleId;
	}

	public int getMemberId() {
		return memberId;
	}

	public void setMemberId(int memberId) {
		this.memberId = memberId;
	}

	public String getBody() {
		return body;
	}

	public void setBody(String body) {
		this.body = body;
	}

}

class Member extends Dto {
	private String loginId;
	private String loginPw;
	private String name;

	public Member() {

	}

	public Member(String loginId, String loginPw, String name) {
		this.loginId = loginId;
		this.loginPw = loginPw;
		this.name = name;
	}

	public String getLoginId() {
		return loginId;
	}

	public void setLoginId(String loginId) {
		this.loginId = loginId;
	}

	public String getLoginPw() {
		return loginPw;
	}

	public void setLoginPw(String loginPw) {
		this.loginPw = loginPw;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
}

// Util
class Util {
// 현재날짜문장
	public static String getNowDateStr() {
		Calendar cal = Calendar.getInstance();
		SimpleDateFormat Date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String dateStr = Date.format(cal.getTime());
		return dateStr;
	}

	public static void deleteFile(String filePath) {
		File file = new File(filePath);
		if (file.exists()) {
			if (file.delete()) {
			} else {
				System.out.println("실패하였습니다.");
			}
		} else {
			System.out.println("존재하지 않습니다.");
		}
	}

// 파일에 내용쓰기
	public static void writeFileContents(String filePath, int data) {
		writeFileContents(filePath, data + "");
	}

// 첫 문자 소문자화
	public static String lcfirst(String str) {
		String newStr = "";
		newStr += str.charAt(0);
		newStr = newStr.toLowerCase();

		return newStr + str.substring(1);
	}

// 파일이 존재하는지
	public static boolean isFileExists(String filePath) {
		File f = new File(filePath);
		if (f.isFile()) {
			return true;
		}

		return false;
	}

// 파일내용 읽어오기
	public static String getFileContents(String filePath) {
		String rs = null;
		try {
// 바이트 단위로 파일읽기
			FileInputStream fileStream = null; // 파일 스트림

			fileStream = new FileInputStream(filePath);// 파일 스트림 생성
// 버퍼 선언
			byte[] readBuffer = new byte[fileStream.available()];
			while (fileStream.read(readBuffer) != -1) {
			}

			rs = new String(readBuffer);

			fileStream.close(); // 스트림 닫기
		} catch (Exception e) {
			e.getStackTrace();
		}

		return rs;
	}

// 파일 쓰기
	public static void writeFileContents(String filePath, String contents) {
		BufferedOutputStream bs = null;
		try {
			bs = new BufferedOutputStream(new FileOutputStream(filePath));
			bs.write(contents.getBytes()); // Byte형으로만 넣을 수 있음
		} catch (Exception e) {
			e.getStackTrace();
		} finally {
			try {
				bs.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

// Json안에 있는 내용을 가져오기
	public static Object getObjectFromJson(String filePath, Class cls) {
		ObjectMapper om = new ObjectMapper();
		Object obj = null;
		try {
			obj = om.readValue(new File(filePath), cls);
		} catch (JsonParseException e) {
			e.printStackTrace();
		} catch (JsonMappingException e) {
			e.printStackTrace();
		} catch (FileNotFoundException e) {

		} catch (IOException e) {
			e.printStackTrace();
		}

		return obj;
	}

	public static void writeJsonFile(String filePath, Object obj) {
		ObjectMapper om = new ObjectMapper();
		try {
			om.writeValue(new File(filePath), obj);
		} catch (JsonGenerationException e) {
			e.printStackTrace();
		} catch (JsonMappingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void makeDir(String dirPath) {
		File dir = new File(dirPath);
		if (!dir.exists()) {
			dir.mkdir();
		}
	}
}