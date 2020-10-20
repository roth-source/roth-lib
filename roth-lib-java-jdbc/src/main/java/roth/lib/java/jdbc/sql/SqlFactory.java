package roth.lib.java.jdbc.sql;

public interface SqlFactory
{
	String LF 				= "\n";
	String END 				= ";";
	String DOT 				= ".";
	String ALL 				= "*";
	String TICK 			= "`";
	String COMMA 			= ", ";
	String EQ 				= " = ";
	String PARAM 			= EQ + "?";
	
	String SELECT 			= " SELECT ";
	String DISTINCT 		= "DISTINCT ";
	String AS 				= " AS ";
	String COLUMN 			= COMMA + LF + "        ";
	String FROM 			= "   FROM ";
	String JOIN				= "   JOIN ";
	String INNER_JOIN		= "  INNER JOIN ";
	String LEFT_JOIN		= "   LEFT JOIN ";
	String RIGHT_JOIN		= "  RIGHT JOIN ";
	String OUTER_JOIN		= "  OUTER JOIN ";
	String ON 				= " ON ";
	String AND 				= " AND ";
	String WHERE			= "  WHERE ";
	String HAVING			= " HAVING ";
	String GROUP_BY 		= "  GROUP BY ";
	String ORDER_BY 		= "  ORDER BY ";
	String ASC 				= " ASC";
	String DESC 			= " DESC";
	String LIMIT 			= "  LIMIT ";
	String OFFSET			= " OFFSET ";
	String INSERT 			= " INSERT INTO ";
	String VALUES 			= " VALUES ";
	String UPDATE 			= " UPDATE ";
	String SET				= "    SET ";
	String DELETE 			= " DELETE ";
	String UNION_ALL 		= "  UNION ALL ";
	String EXISTS 			= "EXISTS ";
	String EXISTS_SQL 		= EXISTS + "(\n%s\n   )";
	String NOT_EXISTS 		= "NOT EXISTS ";
	String NOT_EXISTS_SQL 	= NOT_EXISTS + "(\n%s\n   )";
	String USE_INDEX		= " USE INDEX "+ "(%s)";
	
	String OP_EQ			= " = ?";
	String OP_NE			= " != ?";
	String OP_LT			= " < ?";
	String OP_GT			= " > ?";
	String OP_LE			= " <= ?";
	String OP_GE			= " >= ?";
	String OP_IN			= " IN (%s)";
	String OP_NOT_IN		= " NOT IN (%s)";
	String OP_LIKE			= " LIKE ?";
	String OP_NOT_LIKE		= " NOT LIKE ?";
	String OP_BETWEEN		= " BETWEEN ? AND ?";
	String OP_IS_NULL		= " IS NULL";
	String OP_IS_NOT_NULL	= " IS NOT NULL";
	String LOGIC_AND		= "    AND ";
	String LOGIC_OR			= "     OR ";
	
	Select newSelect();
	Columns newColumns();
	Column newColumn();
	From newFrom();
	Joins newJoins();
	Join newJoin();
	On newOn();
	Wheres newWheres();
	Where newWhere();
	Groups newGroups();
	Group newGroup();
	Havings newHavings();
	Having newHaving();
	Orders newOrders();
	Order newOrder();
	Page newPage();
	Insert newInsert();
	Update newUpdate();
	Set newSet();
	Delete newDelete();
	
}
