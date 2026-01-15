package app;

import util.DateUtil;
import util.StringUtil;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

public class ExpenseTracker {

    final int CSVHeaderCount = 5;

    public record Transaction(UUID id, LocalDate date, String category, BigDecimal amount, String note) {}

    private List<Transaction> transactions = new ArrayList<>();

    public int renameCategory(String oldName, String newName) {
        if (oldName == null || oldName.isBlank() || newName == null || newName.isBlank()) return 0;

        List<Transaction> renamed = transactions.stream().filter(t -> t.category().equalsIgnoreCase(oldName)).toList();

        for(Transaction t : renamed) {
            transactions.remove(t);

            transactions.add(
                new Transaction(
                    t.id(),
                    t.date(),
                    newName,
                    t.amount(),
                    t.note()
                )
            );
        }

        return renamed.size();
    }

    public int deleteAllByCategory(String category) {
        if(category == null || category.isBlank()) return 0;

        //transactions.removeIf(transaction -> transaction.category.equalsIgnoreCase(category));
        List<Transaction> deleted = transactions.stream().filter(t -> t.category().equalsIgnoreCase(category)).toList(); //make sure all objects needs to removed

        transactions.removeAll(deleted);

        return deleted.size();
    }

    public void importFromCSV(String csv, boolean replaceExisting) {
        if (replaceExisting) clearAll();

        importFromCSV(csv);
    }

    public void clearAll() {
        transactions.clear();
    }

    public void importFromCSV(String csv){
        if(csv.isBlank()) return;

        String[] records = csv.split("\n");

        String header = records[0];
        if(!header.equalsIgnoreCase("id,date,category,amount,note")) return; // invalid file format

        if(records.length == 1) return;

        for(int i=1; i<records.length; i++) {
            String[] t = records[i].split(",");

            if(t.length != CSVHeaderCount) throw new IllegalArgumentException("Invalid CSV row at line: " + (i + 1));

            Transaction transaction = new Transaction(
                    UUID.fromString(t[0]),
                    LocalDate.parse(t[1]),
                    t[2],
                    new BigDecimal(t[3]),
                    t[4]
            );

            transactions.add(transaction);
        }
    }

    public String exportToCSV() {
        StringBuilder sb = new StringBuilder();

        //Header
        sb.append("ID,DATE,CATEGORY,AMOUNT,NOTE").append("\n");

        //Transaction
        for(Transaction t : transactions) {
            sb.append(t.id().toString()).append(",");
            sb.append(t.date()).append(",");
            sb.append(StringUtil.csvEscape(t.category())).append(",");
            sb.append(t.amount()).append(",");
            sb.append(StringUtil.csvEscape(t.note())).append("\n");
        }
        return sb.toString();
    }

    public BigDecimal totalSummaryBetweenDates(LocalDate from, LocalDate to) {
        if(transactions.isEmpty()) return BigDecimal.ZERO;

        if( !DateUtil.isNotNullOrEmpty(List.of(from, to)) ) throw new IllegalArgumentException();


        BigDecimal sum = BigDecimal.ZERO;
        for(Transaction t : transactions) {
            if ( !t.date().isBefore(from) && !t.date().isAfter(to) ) {
                sum = sum.add(t.amount());
            }
        }

        return sum;
    }

    public List<Transaction> listTransactionsBetweenDates(LocalDate from, LocalDate to){
        if (transactions.isEmpty()) return List.of();

        if( from == null || to == null || from.isAfter(to) ) {
            throw new IllegalArgumentException();
        }

        return transactions.stream()
                .filter( t -> !t.date().isBefore(from) && !t.date().isAfter(to))
                .collect(Collectors.toList());
    }

    public List<Transaction> listSortedByDateDesc(){
        if (transactions.isEmpty()) return List.of();

        return transactions.stream().sorted(Comparator.comparing(Transaction::date).reversed()).toList();
    }

    public List<Transaction> listTransactionPaged(int page, int size) {
        if(page <= 0 || size <= 0 || ( (page * size) > transactions.size() ) ) {
            return List.of();
        }

        //this copy the elements into new list, slower but safer and slower
//        The stream has:
//        more method calls
//        more indirection
//        more branching
//        This is the overhead.
//        int skipElements = (page - 1) * size;
//        return transactions.stream().skip(skipElements).limit(size).toList(); //this copy the elements into new list, slower but safer

        // this create a view of the original list but faster, no overheads and better for view only. return IndexOutOfBoundsException
        // return transactions.subList( (page - 1), (page * size));

        // this find the elements as fast as sublist but still has stream overhead.
        // return transactions.subList( (page - 1), (page * size)).stream().toList();

        // this fast as sublist then copy the elements into new list without stream overhead.
        int startIndex = (page -1) * size;
        int endIndex = Math.min( (page * size), transactions.size());
        return new ArrayList<Transaction> (transactions.subList(startIndex, endIndex));
    }

    public List<Transaction> listSortedByAmountAsc() {
        if(transactions.isEmpty()) return List.of();

        return transactions.stream().sorted(Comparator.comparing(Transaction::amount)).toList();
    }

    public List<Transaction> listSortedByAmountDesc() {
        if(transactions.isEmpty()) return List.of();

        return transactions.stream().sorted(Comparator.comparing(Transaction::amount).reversed()).toList();
    }

    public Map<String, BigDecimal> summaryTotalByCategoryUsingStreams() {
        if(transactions.isEmpty()) return Collections.EMPTY_MAP;

        return transactions.stream().collect(
                Collectors.toMap(
                        Transaction::category,
                        Transaction::amount,
                        (oldValue,newValue) -> oldValue.add(newValue)
                )
        );
    }

    public List<Transaction> listTransactionsByCategory(String category) {
        if(category == null || category.isBlank()) return List.of();

//        return transactions.stream().filter( t -> t.category().equalsIgnoreCase(category)).toList(); // list is mutable
        return transactions.stream().filter( t -> t.category().equalsIgnoreCase(category)).collect(Collectors.toList()); // list is immutable
    }

    public Map<String, BigDecimal> summaryTotalByCategory() {
        if(transactions.isEmpty()) return Collections.EMPTY_MAP;

        return transactions.stream()
                .collect(
                        Collectors.toMap(
                                Transaction::category,
                                Transaction::amount,
                                (oldValue, newValue) -> oldValue.add(newValue)
                        )
                );
    }

    public BigDecimal summaryTotalAmount() {
        if (transactions.isEmpty()) return new BigDecimal(0);

        BigDecimal sum = BigDecimal.ZERO;
        for(Transaction t : transactions) {
            sum = sum.add(t.amount());
        }

        return sum;
    }

    public boolean updateTransactionNote(UUID id, String newNote) {
        if(id == null) return false;
        if(newNote == null || newNote.equals("")) throw new IllegalArgumentException("Note cannot be empty");

        Optional<Transaction> optionalTransaction = findTransactionById(id);

        if(optionalTransaction.isEmpty()) return false;

        Transaction updatedTransaction = new Transaction(
                optionalTransaction.get().id(),
                optionalTransaction.get().date(),
                optionalTransaction.get().category(),
                optionalTransaction.get().amount(),
                newNote);

        for(int i=0; i<transactions.size(); i++) {
            if(transactions.get(i).id().equals(id)) {
                transactions.set(i, updatedTransaction);
                return true;
            }
        }

        return false;
    }

    public Optional<Transaction> findTransactionById(UUID id) {
        if(id == null) return Optional.empty();

        return transactions.stream().filter( t -> t.id().equals(id)).findAny();
    }

    public boolean removeTransactionById(UUID id) {
        if(id == null) return false;

        return transactions.removeIf( t -> t.id().equals(id));
    }

    public void addTransaction(LocalDate date, String category, BigDecimal amount, String note)
    {
        if(amount != null && amount.compareTo(BigDecimal.ZERO) > 0) {
            transactions.add(new Transaction(UUID.randomUUID(), date, category, amount, note));
        } else {
            throw new IllegalArgumentException("Amount must be greater than 0.");
        }
    }

    public List<Transaction> getTransactions(){
        return new ArrayList<>(transactions);
    }

    public void printHeader(){
        System.out.printf("%-36s  %-10s  %-10s  %12s  %-20s%n",
                "ID", "DATE", "CATEGORY", "AMOUNT", "NOTE");
        System.out.println("-".repeat(95));
    }

    public void printRow(){
        for(Transaction t : transactions) {
            System.out.printf("%-36s  %-10s  %-10s  %12s  %-20s%n",
                    t.id(),
                    t.date(),
                    t.category(),
                    "Rp." + t.amount().setScale(0),
                    t.note()
            );
        }
    }

}
