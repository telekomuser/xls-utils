package com.intech.cms.utils.db;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * Класс для создания {@link Stream} из результатов SQL-запроса.
 * В момент создания потока вместо того, чтобы получать сразу все результаты запроса, настраивается курсор,
 * инкапсулирующий этот запрос, затем результаты запроса передаются по нескольку строк за раз (параметр fetchSize).
 * <p>
 * Полученный Stream обязательно должен быть закрыт: либо вручную, вызовом метода close(),
 * либо используя try-with-resources.
 *
 * @see Stream
 */
public class JDBCStream {
    private static final Logger log = LoggerFactory.getLogger(JDBCStream.class);

    private JDBCStream() {
    }

    public static <T> JDBCStreamBuilder<T> using(DataSource dataSource, Class<T> clazz) {
        return new JDBCStreamBuilder<>(dataSource);
    }

    @FunctionalInterface
    public static interface RowExtractor<T> {
        T extract(ResultSet rs) throws SQLException;
    }

    public static class JDBCStreamBuilder<T> {
        private DataSource dataSource;
        private String sql;
        private Object[] args;
        private RowExtractor<T> extractor;
        private int fetchSize = 1000;

        private JDBCStreamBuilder(DataSource dataSource) {
            this.dataSource = dataSource;
        }

        public JDBCStreamBuilder<T> query(String sql, Object[] args, RowExtractor<T> extractor) {
            this.sql = sql;
            this.args = args;
            this.extractor = extractor;
            return this;
        }

        public JDBCStreamBuilder<T> fetchSize(int size) {
            this.fetchSize = size;
            return this;
        }

        public Stream<T> get() {
            Objects.requireNonNull(dataSource, "DataSource must be set");
            Objects.requireNonNull(sql, "SQL query must be set");
            Objects.requireNonNull(extractor, "Row extractor must be set");

            try {
                ResultSetIterator<T> iterator = new ResultSetIterator<T>(this);

                return StreamSupport.stream(Spliterators.spliteratorUnknownSize(iterator, Spliterator.IMMUTABLE), false)
                        .onClose(iterator::close);
            } catch (SQLException e) {
                log.error("", e);
                throw new RuntimeException(e);
            }
        }
    }

    private static class ResultSetIterator<T> implements Iterator<T> {
        private final Connection connection;
        private final PreparedStatement ps;
        private final ResultSet resultSet;
        private final RowExtractor<T> extractor;

        private ResultSetIterator(JDBCStreamBuilder<T> builder) throws SQLException {
            this.extractor = builder.extractor;
            this.connection = builder.dataSource.getConnection();
            this.connection.setAutoCommit(false);
            this.ps = connection.prepareStatement(builder.sql);
            this.ps.setFetchSize(builder.fetchSize);

            if (builder.args != null) {
                for (int i = 0; i < builder.args.length; i++) {
                    this.ps.setObject(i + 1, builder.args[i]);
                }
            }

            log.debug("execute {}", builder.sql);
            this.resultSet = ps.executeQuery();
        }

        @Override
        public boolean hasNext() {
            try {
                return resultSet.next();
            } catch (SQLException e) {
                log.error("exception in hasNext(): {}", e.toString());
                close(false);
            }

            return false;
        }

        @Override
        public T next() {
            try {
                return extractor.extract(resultSet);
            } catch (Exception e) {
                log.error("exception in next(): {}", e.toString());
                close(false);
            }

            throw new NoSuchElementException();
        }

        private void close() {
            close(true);
        }

        private void close(boolean autoCommit) {
            log.debug("closing all resources");
            try {
                resultSet.close();
                ps.close();

                if (autoCommit) {
                    connection.commit();
                } else {
                    connection.rollback();
                }

                connection.setAutoCommit(true);
                connection.close();
            } catch (SQLException e) {
                log.error("exception while closing: {}", e.toString());
            }
        }
    }

}
