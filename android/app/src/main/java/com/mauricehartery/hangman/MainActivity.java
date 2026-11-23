package com.mauricehartery.hangman;

import android.app.Activity;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import java.util.HashSet;
import java.util.Locale;
import java.util.Random;
import java.util.Set;

public class MainActivity extends Activity {
    private static final int BG = 0xFF101827;
    private static final int CARD = 0xFF19263B;
    private static final int KEY = 0xFF24344C;
    private static final int LINE = 0xFF91A3BD;
    private static final int ACCENT = 0xFF9BE6C1;
    private static final int WRONG = 0xFFFFB7A8;

    private static final String[][] WORDS = {
            {"Nature", "rainbow"}, {"Nature", "mountain"}, {"Nature", "sunflower"},
            {"Food", "pancake"}, {"Food", "avocado"}, {"Food", "blueberry"},
            {"Animals", "penguin"}, {"Animals", "dolphin"}, {"Animals", "hedgehog"},
            {"Places", "library"}, {"Places", "island"}, {"Places", "waterfall"},
            {"Everyday things", "backpack"}, {"Everyday things", "candle"}, {"Everyday things", "umbrella"}
    };

    private final Random random = new Random();
    private final Set<Character> guessed = new HashSet<>();
    private String answer = "";
    private String category = "";
    private int mistakes;
    private boolean finished;
    private TextView categoryView;
    private TextView mistakesView;
    private TextView messageView;
    private TextView wordView;
    private HangmanView drawingView;
    private GridLayout keyboard;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setStatusBarColor(BG);
        getWindow().setNavigationBarColor(BG);
        setContentView(createScreen());
        startGame();
    }

    private View createScreen() {
        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(true);
        scroll.setBackgroundColor(BG);

        LinearLayout page = new LinearLayout(this);
        page.setOrientation(LinearLayout.VERTICAL);
        page.setGravity(Gravity.CENTER_HORIZONTAL);
        page.setPadding(dp(18), dp(22), dp(18), dp(24));
        scroll.addView(page, new ScrollView.LayoutParams(-1, -2));

        TextView eyebrow = text("A LITTLE WORD GAME", 12, ACCENT, true);
        eyebrow.setLetterSpacing(0.16f);
        page.addView(eyebrow, matchWrap());

        TextView title = text("Hangman", 38, 0xFFF4F7FC, true);
        title.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams titleParams = matchWrap();
        titleParams.topMargin = dp(5);
        page.addView(title, titleParams);

        TextView subtitle = text("Guess the hidden word before the drawing is complete.", 15, 0xFFA5B2C5, false);
        subtitle.setGravity(Gravity.CENTER);
        subtitle.setLineSpacing(dp(2), 1f);
        LinearLayout.LayoutParams subtitleParams = matchWrap();
        subtitleParams.setMargins(dp(4), dp(8), dp(4), dp(20));
        page.addView(subtitle, subtitleParams);

        LinearLayout gameCard = new LinearLayout(this);
        gameCard.setOrientation(LinearLayout.VERTICAL);
        gameCard.setPadding(dp(16), dp(16), dp(16), dp(16));
        gameCard.setBackground(rounded(CARD, dp(22), 0xFF34435A, dp(1)));
        page.addView(gameCard, matchWrap());

        LinearLayout status = new LinearLayout(this);
        status.setGravity(Gravity.CENTER_VERTICAL);
        status.setOrientation(LinearLayout.HORIZONTAL);
        categoryView = text("Category: —", 13, 0xFFA5B2C5, false);
        mistakesView = text("Wrong guesses: 0 / 6", 13, 0xFFA5B2C5, false);
        status.addView(categoryView, new LinearLayout.LayoutParams(0, -2, 1f));
        status.addView(mistakesView, wrapWrap());
        gameCard.addView(status, matchWrap());

        drawingView = new HangmanView();
        LinearLayout.LayoutParams drawingParams = matchWrap();
        drawingParams.height = dp(175);
        drawingParams.topMargin = dp(8);
        gameCard.addView(drawingView, drawingParams);

        messageView = text("Pick a letter to get started.", 15, ACCENT, true);
        messageView.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams messageParams = matchWrap();
        messageParams.topMargin = dp(2);
        gameCard.addView(messageView, messageParams);

        wordView = text("", 24, 0xFFF4F7FC, true);
        wordView.setTypeface(Typeface.MONOSPACE, Typeface.BOLD);
        wordView.setGravity(Gravity.CENTER);
        wordView.setSingleLine(false);
        LinearLayout.LayoutParams wordParams = matchWrap();
        wordParams.topMargin = dp(8);
        wordParams.bottomMargin = dp(10);
        gameCard.addView(wordView, wordParams);

        keyboard = new GridLayout(this);
        keyboard.setColumnCount(7);
        keyboard.setAlignmentMode(GridLayout.ALIGN_BOUNDS);
        gameCard.addView(keyboard, matchWrap());

        Button newGame = new Button(this);
        newGame.setText("New game");
        newGame.setTextColor(0xFF13231C);
        newGame.setTextSize(15);
        newGame.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        newGame.setAllCaps(false);
        newGame.setBackground(rounded(ACCENT, dp(12), ACCENT, 0));
        newGame.setOnClickListener(v -> startGame());
        LinearLayout.LayoutParams newGameParams = new LinearLayout.LayoutParams(-2, dp(48));
        newGameParams.gravity = Gravity.CENTER_HORIZONTAL;
        newGameParams.topMargin = dp(14);
        gameCard.addView(newGame, newGameParams);

        TextView hint = text("You can also use your keyboard. Press Enter to start a new game.", 12, 0xFFA5B2C5, false);
        hint.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams hintParams = matchWrap();
        hintParams.topMargin = dp(12);
        gameCard.addView(hint, hintParams);
        return scroll;
    }

    private void startGame() {
        String[] pick;
        do {
            pick = WORDS[random.nextInt(WORDS.length)];
        } while (pick[1].equals(answer));
        category = pick[0];
        answer = pick[1];
        guessed.clear();
        mistakes = 0;
        finished = false;
        categoryView.setText("Category: " + category);
        mistakesView.setText("Wrong guesses: 0 / 6");
        messageView.setText("Pick a letter to get started.");
        messageView.setTextColor(ACCENT);
        refreshGame();
    }

    private void guess(char letter) {
        if (finished || guessed.contains(letter)) return;
        guessed.add(letter);
        if (answer.indexOf(letter) < 0) {
            mistakes++;
            mistakesView.setText("Wrong guesses: " + mistakes + " / 6");
        }
        boolean won = true;
        for (char character : answer.toCharArray()) {
            if (!guessed.contains(character)) {
                won = false;
                break;
            }
        }
        if (won) {
            finished = true;
            messageView.setText("You got it! Nice guessing.");
        } else if (mistakes >= 6) {
            finished = true;
            messageView.setText("Game over — the word was " + answer + ".");
            messageView.setTextColor(WRONG);
        } else {
            messageView.setText(answer.indexOf(letter) >= 0 ? "Good guess!" : "Not quite — try another letter.");
        }
        refreshGame();
    }

    private void refreshGame() {
        drawingView.setMistakes(mistakes);
        StringBuilder masked = new StringBuilder();
        for (char letter : answer.toCharArray()) {
            if (masked.length() > 0) masked.append("   ");
            masked.append(guessed.contains(letter) || finished ? Character.toUpperCase(letter) : '_');
        }
        wordView.setText(masked.toString());
        keyboard.removeAllViews();
        for (char letter = 'A'; letter <= 'Z'; letter++) {
            final char current = Character.toLowerCase(letter);
            Button keyButton = new Button(this);
            keyButton.setText(String.valueOf(letter));
            keyButton.setTextSize(14);
            keyButton.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
            keyButton.setTextColor(0xFFF4F7FC);
            keyButton.setAllCaps(false);
            keyButton.setMinWidth(0);
            keyButton.setMinimumWidth(0);
            keyButton.setPadding(0, 0, 0, 0);
            boolean used = guessed.contains(current);
            keyButton.setEnabled(!used && !finished);
            int keyColor = used ? (answer.indexOf(current) >= 0 ? 0xFF245A48 : 0xFF563B43) : KEY;
            keyButton.setBackground(rounded(keyColor, dp(10), used ? keyColor : 0xFF44556F, dp(1)));
            keyButton.setContentDescription(String.valueOf(letter));
            keyButton.setOnClickListener(v -> guess(current));
            GridLayout.LayoutParams keyParams = new GridLayout.LayoutParams(
                    GridLayout.spec(GridLayout.UNDEFINED, 1f),
                    GridLayout.spec(GridLayout.UNDEFINED));
            keyParams.width = 0;
            keyParams.height = dp(42);
            keyParams.setMargins(dp(2), dp(2), dp(2), dp(2));
            keyboard.addView(keyButton, keyParams);
        }
        drawingView.invalidate();
    }

    private TextView text(String value, float size, int color, boolean bold) {
        TextView view = new TextView(this);
        view.setText(value);
        view.setTextSize(size);
        view.setTextColor(color);
        view.setTypeface(Typeface.DEFAULT, bold ? Typeface.BOLD : Typeface.NORMAL);
        return view;
    }

    private GradientDrawable rounded(int color, int radius, int strokeColor, int strokeWidth) {
        GradientDrawable shape = new GradientDrawable();
        shape.setColor(color);
        shape.setCornerRadius(radius);
        if (strokeWidth > 0) shape.setStroke(strokeWidth, strokeColor);
        return shape;
    }

    private LinearLayout.LayoutParams matchWrap() {
        return new LinearLayout.LayoutParams(-1, -2);
    }

    private LinearLayout.LayoutParams wrapWrap() {
        return new LinearLayout.LayoutParams(-2, -2);
    }

    private int dp(float value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }

    private final class HangmanView extends View {
        private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private int parts;

        HangmanView() {
            super(MainActivity.this);
            setContentDescription("Hangman drawing");
        }

        void setMistakes(int value) {
            parts = value;
            setContentDescription("Hangman drawing, " + value + " wrong guesses");
        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            float scale = Math.min(getWidth() / 220f, getHeight() / 190f);
            float left = (getWidth() - 220f * scale) / 2f;
            float top = (getHeight() - 190f * scale) / 2f;
            canvas.save();
            canvas.translate(left, top);
            canvas.scale(scale, scale);
            paint.setColor(LINE);
            paint.setStrokeWidth(6f);
            paint.setStrokeCap(Paint.Cap.ROUND);
            paint.setStyle(Paint.Style.STROKE);
            canvas.drawLine(28, 176, 166, 176, paint);
            canvas.drawLine(62, 176, 62, 22, paint);
            canvas.drawLine(62, 22, 152, 22, paint);
            canvas.drawLine(152, 22, 152, 45, paint);
            canvas.drawLine(62, 52, 91, 22, paint);
            paint.setColor(WRONG);
            if (parts >= 1) canvas.drawCircle(152, 62, 17, paint);
            if (parts >= 2) canvas.drawLine(152, 79, 152, 122, paint);
            if (parts >= 3) canvas.drawLine(152, 90, 127, 111, paint);
            if (parts >= 4) canvas.drawLine(152, 90, 177, 111, paint);
            if (parts >= 5) canvas.drawLine(152, 122, 130, 152, paint);
            if (parts >= 6) canvas.drawLine(152, 122, 174, 152, paint);
            canvas.restore();
        }
    }
}

