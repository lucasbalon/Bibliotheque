import lombok.Data;

import java.time.LocalDate;
import java.util.*;

@Data
public class Library {
    private ArrayList<Book> bookList = new ArrayList<>();
    private ArrayList<User> userList = new ArrayList<>();
    private ArrayList<Borrow> borrowsHistoric = new ArrayList<>();
    private ArrayList<Author> authorList = new ArrayList<>();
    private Scanner sc = new Scanner(System.in);

    public void addBook(Book book, User user) {
        verifyRole(user, Role.ADMIN);
        bookList.forEach(book1 -> {
            if (Objects.equals(book.getISBN(), book1.getISBN())) {
                throw new IllegalArgumentException("Le livre avec l'ISBN " + book.getISBN() + " existe déjà");
            }
        });
        bookList.add(book);
    }
    public void deleteBook(Book book, User user) {
        verifyRole(user, Role.ADMIN);
        bookList.removeIf(book1 -> book.getISBN().equals(book1.getISBN()));
    }
    public void modifyBook(Book originalBook, Book updatedBook, User user) {
        verifyRole(user, Role.ADMIN);
        bookList.forEach(book -> {
            if (originalBook.getISBN().equals(updatedBook.getISBN())) {
                book.setTitle(updatedBook.getTitle());
                book.setAuthor(updatedBook.getAuthor());
                book.setYearOfPublication(updatedBook.getYearOfPublication());
            }
        });
    }
    public void displayBooks() {
        bookList.forEach(System.out::println);
    }
    public void displayAvailableBooks() {
        System.out.println("Livres disponibles: ");
        bookList.stream()
                .filter(Book::isAvailable)
                .forEach(System.out::println);
    }
    public void borrowBook(Book book, User user) {
        if (book.isAvailable()) {
            borrowsHistoric.add(new Borrow(99, book, user)); // TODO: 26/09/2023 gérer les id avec la DB ?
            book.setAvailable(false);
        } else {
            throw new NoSuchElementException("Livre inexistant ou non disponible");
        }

    }
    public void returnBook(Book book, User user) {
        borrowsHistoric.forEach(borrow -> {
            if (borrow.getBook().equals(book) && borrow.getUser().equals(user)) {
                book.setAvailable(true);
                borrow.setEndDate(LocalDate.now());
            } else {
                throw new NoSuchElementException("Le livre voulant être retourné n'a pas été emprunté");
            }
        });
    }
    public void verifyRole(User user, Role role) {
        if (user.getRole() != role) {
            throw new SecurityException("Action non autorisée: l'utilisateur doit être " + role);
        }
    }

    public Book findByISBN(String ISBN) {
        return bookList.stream()
                .filter(book -> ISBN.equals(book.getISBN()))
                .findFirst()
                .orElseThrow(() -> new NoSuchElementException("Mauvais ISBN"));
    }

    public User connect(String login, String password) {
        return userList.stream()
                .filter(user -> login.equals(user.getLogin()) && password.equals(user.getPassword()))
                .findFirst()
                .orElseThrow(() -> new NoSuchElementException("Identifiant ou mot de passe erroné"));
    }

    public Book encodeBook() {
       if (true){
           System.out.println("Titre: ");
           String title = sc.nextLine();
           System.out.println("Auteur: ");
           String author = sc.nextLine();
           System.out.println("Année de publication: ");
           int year = Integer.parseInt(sc.nextLine());
           System.out.println("ISBN: ");
           String ISBN = sc.nextLine();

           return new Book(99,title,Author.builder().idAuthor(99).name(author).build(), year,ISBN); // TODO: 26/09/2023 créer idBook avec DB ? + pour author

       }else {
           throw new IllegalArgumentException("Mauvais format");
       }
    }

    public void init() {
        Requetes.initAuthors(authorList);
        Requetes.initUsers(userList);
        Requetes.initBooks(bookList,authorList);
        Requetes.initBorrow(borrowsHistoric, bookList, userList);
    }

    public boolean start() {
        init();

        String connect = """
                *********************
                *   Bibliothèque    *
                *********************
                login:""";
        String menuAdmin = """
                *********************
                *   Bibliothèque    *
                *********************
                Admin mode
                1. Afficher les livres disponibles
                2. Afficher les informations d'un livre
                3. Emprunter un livre
                4. Retourner un livre
                5. Ajouter un livre
                6. Modifier un livre
                7. Supprimer un livre
                
                0. Quitter
                """;
        String menu = """
                *********************
                *   Bibliothèque    *
                *********************
                1. Afficher les livres disponibles
                2. Afficher les informations d'un livre
                3. Emprunter un livre
                4. Retourner un livre
                
                0. Quitter
                """;
        User user;
        try {
            String login, password;
            System.out.println(connect);
            login = sc.nextLine();
            System.out.println("password: ");
            password = sc.nextLine();
            user = connect(login,password);
        }catch (Exception e) {
            System.out.println(e);
            return false;
        }

        if (user.getRole().equals(Role.ADMIN)) {
            menu = menuAdmin;
        }

        int choice;
        while (true) {
            try {
                System.out.println(menu);
                choice = Integer.parseInt(sc.nextLine());

                switch (choice) {
                    case 0:
                        return true;
                    case 1:
                        displayAvailableBooks();
                        break;
                    case 2:
                        displayBooks();
                        System.out.println("Choisir par ISBN: ");
                        findByISBN(sc.nextLine()).displayAll();
                        break;
                    case 3:
                        displayAvailableBooks();
                        System.out.println("Quel livre emprunter (ISBN)?");
                        borrowBook(findByISBN(sc.nextLine()), user);
                        break;
                    case 4:
                        displayBooks();
                        System.out.println("Quel livre souhaitez-vous rendre");
                        returnBook(findByISBN(sc.nextLine()), user);
                        break;
                    case 5:
                        addBook(encodeBook(), user);
                        break;
                    case 6:
                        displayBooks();
                        System.out.println("Quel livre modifier ? : ");
                        modifyBook(findByISBN(sc.nextLine()), encodeBook(), user);
                        break;
                    case 7:
                        displayBooks();
                        System.out.println("Quel livre supprimer ? : ");
                        deleteBook(findByISBN(sc.nextLine()), user);
                }
            }catch (Exception e) {
                System.out.println(e.getMessage());
            }
        }
    }
    
}
