/*******************************************************************************
 * Copyright (c) 2017-2019 Maschell
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *******************************************************************************/

package de.mas.bots.discordbot.persistence;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.mas.bots.discordbot.RoleCommandInfo;
import lombok.val;

public class RolePersistence {
    private static final String TABLE_NAME = "role_commands";

    private static final Map<String, RolePersistence> instances = new HashMap<>();

    public static RolePersistence getInstance(String databasePath) throws SQLException {
        if (!instances.containsKey(databasePath)) {
            instances.put(databasePath, new RolePersistence(databasePath));
        }
        return instances.get(databasePath);
    }

    private final Connection connection;

    public RolePersistence(String databasePath) throws SQLException {
        this.connection = DriverManager.getConnection("jdbc:sqlite:" + databasePath);
        Statement statement = connection.createStatement();
        try {
            statement.executeQuery("select * from " + TABLE_NAME);
        } catch (SQLException e1) {
            statement.executeUpdate("create table " + TABLE_NAME + " (id integer, alias string,roleid long)");
        }
    }

    public List<RoleCommandInfo> getAll() {
        val result = new ArrayList<RoleCommandInfo>();
        try {
            PreparedStatement stmnt = connection.prepareStatement("select id,alias,roleid from " + TABLE_NAME + " ORDER BY ID ASC");
            ResultSet rs = stmnt.executeQuery();
            while (rs.next()) {
                int id = rs.getInt("id");
                String alias = rs.getString("alias");
                long roleid = rs.getLong("roleid");
                result.add(new RoleCommandInfo(id, alias, roleid));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return result;
    }

    public void add(RoleCommandInfo info) {
        try {
            PreparedStatement stmnt = connection.prepareStatement("insert into " + TABLE_NAME + " values(?, ?,?)");
            stmnt.setInt(1, info.getID());
            stmnt.setString(2, info.getAlias());
            stmnt.setLong(3, info.getRoleID());
            stmnt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void remove(RoleCommandInfo info) {
        remove(info.getID());
    }

    public void remove(int cmdID) {
        try {
            PreparedStatement stmnt = connection.prepareStatement("delete from " + TABLE_NAME + " where id = ?");
            stmnt.setInt(1, cmdID);
            stmnt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

}
