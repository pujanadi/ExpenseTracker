package app;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class ExpenseTrackerTest {

    ExpenseTracker tracker;

    @BeforeEach
    void setup() {
        tracker = new ExpenseTracker();

        tracker.addTransaction(
                LocalDate.now().minusDays(1),
                "Food",
                new BigDecimal("23000.00"),
                "Rokok di warung madura"
        );

    }

    @Test
    void addTransaction_shouldAddItem() {
        ExpenseTracker t = new ExpenseTracker();

        t.addTransaction(
                LocalDate.now().minusDays(1),
                "Food",
                new BigDecimal("23000.00"),
                "Rokok di warung madura"
        );

        var txs = t.getTransactions();
        assertEquals(1, txs.size());
        assertEquals("Food", txs.get(0).category());
        assertEquals(new BigDecimal("23000.00"), txs.get(0).amount());
    }

    @Test
    void addTransaction_amountZero_shouldThrow(){
        ExpenseTracker t = new ExpenseTracker();

        var e = assertThrows(
                IllegalArgumentException.class,
                () -> t.addTransaction(
                    LocalDate.now().minusDays(1),
                    "Food",
                    new BigDecimal("0"),
                    "Rokok di warung madura"
                )
                ,"Expected validate amount to throw, but it didn't"
        );

        assertTrue(e.getMessage().contains("Amount must be greater than 0."));
    }

    @Test
    public void removeTransaction_shouldRemove(){
        var txs = tracker.getTransactions();
        assertEquals(1, txs.size());

        assertEquals(true, tracker.removeTransactionById(txs.get(0).id()));

        txs = tracker.getTransactions();
        assertEquals(0, txs.size());
    }

    @Test
    public void removeTransaction_idNotFound(){
        assertEquals(false, tracker.removeTransactionById(UUID.randomUUID()));
    }

    @Test
    public void findTransactionById_shouldReturn(){
        var id = tracker.getTransactions().get(0).id();
        var t = tracker.findTransactionById(id);

        assertTrue(t.isPresent());
        assertEquals(id, t.get().id());
    }

    @Test
    public void findTransactionById_notFound() {
        var id = UUID.randomUUID();
        var optionalTransaction = tracker.findTransactionById(id);

        assertFalse(optionalTransaction.isPresent());
        assertTrue(optionalTransaction.isEmpty());
    }

    @Test
    public void updateTransactionNote_shouldTrue() {
        var id = tracker.getTransactions().get(0).id();
        String newNote = "Updated Note";

        tracker.updateTransactionNote(id, newNote);
        var transaction = tracker.findTransactionById(id);

        assertEquals(newNote, transaction.get().note());
    }

    @Test
    public void updateTransactionNote_shouldFalse() {
        var id = tracker.getTransactions().get(0).id();
        var note = tracker.getTransactions().get(0).note();
        String newNote = "Updated Note";

        assertFalse(tracker.updateTransactionNote(UUID.randomUUID(), newNote));

        var transaction = tracker.findTransactionById(id);
        assertEquals(note, transaction.get().note());
    }

    @Test
    public void updateTransactionNote_inputEmptyString_shouldThrowError() {
        var id = tracker.getTransactions().get(0).id();
        String newNote = "";

        var ex = assertThrows(
                IllegalArgumentException.class,
                () -> tracker.updateTransactionNote(id, newNote)
        );

        assertEquals("Note cannot be empty", ex.getMessage());
    }

    @Test
    public void updateTransactionNote_inputNull_shouldThrowError() {
        var id = tracker.getTransactions().get(0).id();
        String newNote = null;

        assertThrows(
                IllegalArgumentException.class,
                () -> tracker.updateTransactionNote(id, newNote)
        );
    }

    @Test
    void summaryTotalAmount_shouldSum(){
        tracker.addTransaction(
                LocalDate.now().minusDays(1),
                "Transport",
                new BigDecimal("7000.00"),
                "gojek"
        );

        assertEquals(new BigDecimal("30000.00"), tracker.summaryTotalAmount());
    }

    @Test
    void summaryTotalAmount_inputEmptyTrx_shouldShowZero(){
        ExpenseTracker t = new ExpenseTracker();
        assertEquals(new BigDecimal(0), t.summaryTotalAmount());
    }

    @Test
    void summaryTotalByCategory_shouldShowAmount(){
        // default list for Food is 27000.00
        tracker.addTransaction(
                LocalDate.now().minusDays(1),
                "Food",
                new BigDecimal("7000.00"),
                "Cilok"
        );

        // add new category
        tracker.addTransaction(
                LocalDate.now().minusDays(1),
                "Transport",
                new BigDecimal("7000.00"),
                "gojek"
        );

        var m = tracker.summaryTotalByCategory();
        assertEquals(new BigDecimal("30000.00"), m.get("Food"));
        assertEquals(new BigDecimal("7000.00"), m.get("Transport"));
    }

    @Test
    void summaryTotalByCategory_emptyList_shouldReturnZero() {
        ExpenseTracker t = new ExpenseTracker();

        var map = t.summaryTotalByCategory();
        assertEquals(0, map.size());
    }

    @Test
    void listTransactionsByCategory_shouldReturnList() {
        var list = tracker.listTransactionsByCategory("food");
        assertEquals("Food", list.get(0).category());
    }

    @Test
    void listTransactionsByCategory_inputEmptyString_shouldEmptyList() {
        var list = tracker.listTransactionsByCategory("");
        assertTrue(list.isEmpty());
    }

    @Test
    void summaryTotalByCategoryUsingStreams_shouldReturnList() {
        tracker.addTransaction(
                LocalDate.now().minusDays(1),
                "Food",
                new BigDecimal("7000.00"),
                "gojek"
        );

        tracker.addTransaction(
                LocalDate.now().minusDays(1),
                "Transport",
                new BigDecimal("3000.00"),
                "teh kotak"
        );

        var map = tracker.summaryTotalByCategoryUsingStreams();

        assertEquals(new BigDecimal("30000.00"), map.get("Food"));
        assertEquals(new BigDecimal("3000.00"), map.get("Transport"));
    }

    @Test
    void summaryTotalByCategoryUsingStreams_inputEmptyList_shouldReturnList() {
        ExpenseTracker t = new ExpenseTracker();

        var map = t.summaryTotalByCategoryUsingStreams();

        assertTrue(map.isEmpty());
    }

    @Test
    void listSortedByAmountAsc_shouldReturnList() {
        tracker.addTransaction(
                LocalDate.now().minusDays(1),
                "Food",
                new BigDecimal("7000.00"),
                "gojek"
        );

        var list = tracker.listSortedByAmountAsc();
        assertEquals(new BigDecimal("7000.00"), list.get(0).amount());
    }

    @Test
    void listSortedByAmountDesc_shouldReturnList() {
        tracker.addTransaction(
                LocalDate.now().minusDays(1),
                "Food",
                new BigDecimal("7000.00"),
                "gojek"
        );

        var list = tracker.listSortedByAmountDesc();
        assertEquals(new BigDecimal("23000.00"), list.get(0).amount());
    }

    @Test
    void listSortedByAmountAsc_shouldNotMutateOriginalOrder() {
        ExpenseTracker t = new ExpenseTracker();

        t.addTransaction(LocalDate.now(), "A", new BigDecimal("2.00"), "t1");
        t.addTransaction(LocalDate.now(), "B", new BigDecimal("1.00"), "t2");

        t.listSortedByAmountAsc();

        // original order should still be insertion order
        var original = t.getTransactions();
        assertEquals("t1", original.get(0).note());
        assertEquals("t2", original.get(1).note());
    }

    @Test
    void listTransactionPaged_shouldReturn() {
        tracker.addTransaction(LocalDate.now(), "A", new BigDecimal("2.00"), "t2");
        tracker.addTransaction(LocalDate.now(), "B", new BigDecimal("1.00"), "t3");
        tracker.addTransaction(LocalDate.now(), "C", new BigDecimal("1.00"), "t4");
        tracker.addTransaction(LocalDate.now(), "D", new BigDecimal("1.00"), "t5");

        int page = 1;
        int size = 5;
        assertEquals(5, tracker.listTransactionPaged(page, size).size());
    }

    @Test
    void listTransactionPaged_shouldReturnOneElement() {
        tracker.addTransaction(LocalDate.now(), "A", new BigDecimal("2.00"), "t2");
        tracker.addTransaction(LocalDate.now(), "B", new BigDecimal("1.00"), "t3");
        tracker.addTransaction(LocalDate.now(), "C", new BigDecimal("1.00"), "t4");
        tracker.addTransaction(LocalDate.now(), "D", new BigDecimal("1.00"), "t5");
        tracker.addTransaction(LocalDate.now(), "D", new BigDecimal("1.00"), "t6");

        int page = 2;
        int size = 5;
        assertEquals(1, tracker.listTransactionPaged(page, size).size());
    }

    @Test
    void listTransactionPaged_inputPageMoreThanTotalElements_shouldReturnEmptyList() {
        tracker.addTransaction(LocalDate.now(), "A", new BigDecimal("2.00"), "t2");
        tracker.addTransaction(LocalDate.now(), "B", new BigDecimal("1.00"), "t3");
        tracker.addTransaction(LocalDate.now(), "C", new BigDecimal("1.00"), "t4");
        tracker.addTransaction(LocalDate.now(), "D", new BigDecimal("1.00"), "t5");
        tracker.addTransaction(LocalDate.now(), "D", new BigDecimal("1.00"), "t6");

        int page = 3;
        int size = 5;
        assertEquals(0, tracker.listTransactionPaged(page, size).size());
    }

    @Test
    void listSortedByDateDesc_shouldReturnSortedByDate(){
        tracker.addTransaction(LocalDate.now(), "A", new BigDecimal("2.00"), "t2");
        tracker.addTransaction(LocalDate.now().minusMonths(3), "B", new BigDecimal("1.00"), "t3");
        tracker.addTransaction(LocalDate.now().minusDays(4), "C", new BigDecimal("1.00"), "t4");

        var sorted = tracker.listSortedByDateDesc();

        // t2 -> t1 -> t4 -> t3
        assertEquals("t2", sorted.get(0).note());
        assertEquals("Rokok di warung madura", sorted.get(1).note());
        assertEquals("t4", sorted.get(2).note());
        assertEquals("t3", sorted.get(3).note());
    }

    @Test
    void listSortedByDateDesc_inputEmptyTransaction_shouldReturnEmptyList(){
        ExpenseTracker t = new ExpenseTracker();

        var sorted = t.listSortedByDateDesc();

        assertTrue(sorted.isEmpty());
    }

    @Test
    void listTransactionsBetweenDates_shouldReturnDateBetween() {
        tracker.updateTransactionNote(tracker.getTransactions().getFirst().id(), "t1");
        tracker.addTransaction(LocalDate.now(), "A", new BigDecimal("2.00"), "t2");
        tracker.addTransaction(LocalDate.now().minusDays(3), "B", new BigDecimal("1.00"), "t3");
        tracker.addTransaction(LocalDate.now().minusDays(4), "C", new BigDecimal("1.00"), "t4");

        LocalDate from = LocalDate.now().minusDays(2);
        LocalDate to = LocalDate.now();
        var sorted = tracker.listTransactionsBetweenDates(from, to);

        System.out.printf("%-10s %-10s%n","from:" + from, "to:"+to);
        for(ExpenseTracker.Transaction t : sorted) { System.out.println(t); }

        assertEquals(2, sorted.size());
    }

    @Test
    void listTransactionsBetweenDates_invalidInput_shouldReturnIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> tracker.listTransactionsBetweenDates(null, LocalDate.now()));
        assertThrows(IllegalArgumentException.class, () -> tracker.listTransactionsBetweenDates(LocalDate.now(), null));
        assertThrows(IllegalArgumentException.class, () -> tracker.listTransactionsBetweenDates(null, null));
        assertThrows(IllegalArgumentException.class, () -> tracker.listTransactionsBetweenDates(LocalDate.now().plusDays(1), LocalDate.now()));
        assertThrows(IllegalArgumentException.class, () -> tracker.listTransactionsBetweenDates(LocalDate.now(), LocalDate.now().minusMonths(1)));
    }
}
