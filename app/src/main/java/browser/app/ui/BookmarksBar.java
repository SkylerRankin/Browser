package browser.app.ui;

import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;

public class BookmarksBar extends HBox {
    
    private TextField urlInput;
    
    public BookmarksBar(TextField urlInput) {
        this.urlInput = urlInput;
        this.setPrefHeight(50);
        addBookmark("https://www.google.com");
        addBookmark("file://res/html/table.html");
        addBookmark("file://res/html/startup_page.html");
//        addBookmark("http://gallium.inria.fr/~fpottier/menhir/");
//        addBookmark("https://caml.inria.fr/pub/docs/manual-ocaml/libref/Option.html");
//        addBookmark("https://caml.inria.fr/pub/docs/manual-ocaml/libref/List.html");
//        addBookmark("https://www.kernel.org/doc/man-pages/");
//        addBookmark("https://www.kernel.org/doc/man-pages/download.html");
    }
    
    public void addBookmark(String url) {
        Button button = new Button(url);
        button.getStyleClass().add("bookmark_button");
        button.setOnAction(event -> {
            urlInput.setText(url);
            KeyEvent enter = new KeyEvent(KeyEvent.KEY_PRESSED, "", "", KeyCode.ENTER, false, false, false, false);
            urlInput.fireEvent(enter);
        });
        getChildren().add(button);
    }

}
