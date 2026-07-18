package com.nokhrin.accounting.infrastructure.persistence;

import com.nokhrin.accounting.domain.account.Account;
import com.nokhrin.accounting.domain.account.AccountHolder;
import com.nokhrin.accounting.domain.account.AccountRepository;
import com.nokhrin.accounting.domain.account.AccountStatus;

import javax.sql.DataSource;
import java.sql.*;
import java.util.Optional;
import java.util.UUID;

public class JdbcAccountRepository implements AccountRepository {
    private final DataSource dataSource;

    public JdbcAccountRepository(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public void save(Account account) {
        String createAccountQuery = """
                INSERT INTO accounts (id, balance, status, holder_id, holder_display_name)
                VALUES (?, ?, ?, ?, ?);
                """;

        try (
                Connection connection = dataSource.getConnection();
                PreparedStatement preparedStatement = connection.prepareStatement(createAccountQuery)
        ) {
            preparedStatement.setObject(1, account.id());
            preparedStatement.setObject(2, account.balance());
            preparedStatement.setObject(3, account.status().name());
            preparedStatement.setObject(4, account.holder().id());
            preparedStatement.setObject(5, account.holder().displayName());

            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Create account failed", e);
        }

    }

    @Override
    public Optional<Account> findById(UUID id) {
        String selectByIdQuery = """
                SELECT id, balance, status, holder_id, holder_display_name
                FROM public.accounts where id = ?;
                """;
        try (
                Connection connection = dataSource.getConnection();
                PreparedStatement preparedStatement = connection.prepareStatement(selectByIdQuery)
        ) {
            preparedStatement.setObject(1, id);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    return Optional.of(mapToDomain(resultSet));
                }
                return Optional.empty();
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find account by id: " + id, e);
        }
    }

    private Account mapToDomain(ResultSet resultSet) throws SQLException {
        return new Account(
                resultSet.getObject("id", UUID.class),
                resultSet.getBigDecimal("balance"),
                AccountStatus.valueOf(resultSet.getString("status")),
                new AccountHolder(
                        resultSet.getObject("holder_id", UUID.class),
                        resultSet.getString("holder_display_name")
                )
        );
    }
}
