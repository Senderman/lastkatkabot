package com.senderman.lastkatkabot.feature.data.command;

import com.senderman.lastkatkabot.Role;
import com.senderman.lastkatkabot.command.CommandExecutor;
import com.senderman.lastkatkabot.feature.l10n.context.L10nMessageContext;
import io.micronaut.data.connection.annotation.Connectable;
import jakarta.inject.Singleton;
import org.jetbrains.annotations.NotNull;

import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.Objects;
import java.util.stream.Collectors;

@Singleton
public class RunSqlCommand implements CommandExecutor {

    private final DataSource dataSource;

    public RunSqlCommand(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public String command() {
        return "/sql";
    }

    @Override
    public String getDescription() {
        return "sql.description";
    }

    @Override
    public EnumSet<Role> authority() {
        return EnumSet.of(Role.MAIN_ADMIN);
    }

    @Override
    public void accept(@NotNull L10nMessageContext ctx) {
        ctx.setArgumentsLimit(1);
        if (ctx.argumentsLength() < 1) {
            ctx.replyToMessage(ctx.getString("common.invalidArgumentsNumber")).callAsync(ctx.sender);
            return;
        }
        var query = ctx.argument(0);
        var firstKeyword = query.split("\\s+", 2)[0].toUpperCase();
        switch (firstKeyword) {
            case "SELECT" -> ctx.replyToMessage(executeQuery(query)).callAsync(ctx.sender);
            case "INSERT", "UPDATE", "DELETE" ->
                    ctx.replyToMessage(String.valueOf(executeUpdate(query))).callAsync(ctx.sender);
            default -> ctx.replyToMessage(String.valueOf(executeAny(query))).callAsync(ctx.sender);
        }
    }

    @Connectable
    public boolean executeAny(String query) {
        try (var conn = dataSource.getConnection()) {
            return conn.prepareStatement(query).execute();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Connectable
    public int executeUpdate(String query) {
        try (var conn = dataSource.getConnection()) {
            return conn.prepareStatement(query).executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Connectable
    public String executeQuery(String query) {
        try (var conn = dataSource.getConnection()) {
            var rs = conn.
                    prepareStatement(query, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY)
                    .executeQuery();
            return formatResultSet(rs);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

    }

    private String formatResultSet(ResultSet rs) throws SQLException {
        var meta = rs.getMetaData();
        var sb = new StringBuilder("<code>");
        int totalColumns = meta.getColumnCount();
        int[] colWidth = new int[totalColumns];
        for (int i = 0; i < totalColumns; i++) {
            colWidth[i] = getMaxColumnSize(rs, i + 1);
            sb.append(String.format("| %" + colWidth[i] + "s", meta.getColumnLabel(i + 1)));
        }
        String horizontalLine = getHorizontalLine(colWidth);
        sb.append("|\n").append(horizontalLine);
        rs.beforeFirst();
        while (rs.next()) {
            for (int i = 0; i < totalColumns; i++) {
                sb.append(String.format("| %" + colWidth[i] + "s", rs.getString(i + 1)));
            }
            sb.append("|\n").append(horizontalLine);
        }
        return sb.append("</code>").toString();
    }

    int getMaxColumnSize(ResultSet rs, int columnIndex) throws SQLException {
        int maxSize = rs.getMetaData().getColumnLabel(columnIndex).length();
        rs.beforeFirst();
        while (rs.next()) {
            int length = Objects.requireNonNullElse(rs.getString(columnIndex), "").length();
            if (length > maxSize)
                maxSize = length;
        }
        return maxSize;
    }

    private String getHorizontalLine(int[] colWidth) {
        return Arrays.stream(colWidth)
                .mapToObj(width -> "|" + "-".repeat(width + 1))
                .collect(Collectors.joining("", "", "|\n"));
    }


}
