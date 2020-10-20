package roth.lib.java.jdbc.sql;

import java.util.Collection;
import roth.lib.java.lang.List;

@SuppressWarnings("serial")
public abstract class Select extends Sql implements SqlFactory
{
	protected boolean distinct;
	protected Columns columns;
	protected From from;
	protected Joins joins;
	protected Wheres wheres;
	protected Groups groups;
	protected Havings havings;
	protected Orders orders;
	protected Page page;
	
	protected List<Wheres> nestedWheres = new List<Wheres>();
	protected List<Havings> nestedHavings = new List<Havings>();
	
	protected Select()
	{
		
	}
	
	public Select columns(Columns columns)
	{
		this.columns = columns;
		return this;
	}
	
	public Select column(Column...columns)
	{
		if(this.columns == null)
		{
			this.columns = newColumns();
		}
		this.columns.addColumns(columns);
		return this;
	}
	
	public Select distinct()
	{
		distinct = true;
		return this;
	}
	
	public Select columnAll(String table)
	{
		return column(newColumn().setTable(table).setName(ALL));
	}
	
	public Select columnSql(String sql)
	{
		return column(newColumn().setSql(sql));
	}
	
	public Select columnSqlAs(String sql, String alias)
	{
		return column(newColumn().setSql(sql).setAlias(alias));
	}
	
	public Select column(String name)
	{
		return column(newColumn().setName(name));
	}
	
	public Select columnAs(String name, String alias)
	{
		return column(newColumn().setName(name).setAlias(alias));
	}
	
	public Select column(String table, String name)
	{
		return column(newColumn().setTable(table).setName(name));
	}
	
	public Select columnAs(String table, String name, String alias)
	{
		return column(newColumn().setTable(table).setName(name).setAlias(alias));
	}
	
	public Select columnExistsAs(Select select, String alias)
	{
		return column(newColumn().setSql(EXISTS_SQL).setSelect(select).setAlias(alias));
	}
	
	public Select columnNotExistsAs(Select select, String alias)
	{
		return column(newColumn().setSql(NOT_EXISTS_SQL).setSelect(select).setAlias(alias));
	}
	
	public Select from(From from)
	{
		this.from = from;
		return this;
	}
	
	public Select from(String name)
	{
		return from(newFrom().setName(name));
	}
	
	public Select fromAs(String name, String alias)
	{
		return from(newFrom().setName(name).setAlias(alias));
	}
	
	public Select fromAs(Select select, String alias)
	{
		return from(newFrom().setSelect(select).setAlias(alias));
	}
	
	public Select fromUnionAllAs(String alias, Select...selects)
	{
		return from(newFrom().setUnionAllSelects(selects).setAlias(alias));
	}
	
	public Select joins(Joins joins)
	{
		this.joins = joins;
		return this;
	}
	
	public Select join(Join join)
	{
		joins = joins != null ? joins : newJoins();
		joins.addJoins(join);
		return this;
	}
	
	public Select join(String table1, String name1, String table2, String name2)
	{
		return join(newJoin().setJoinType(JOIN).setTable(table1).addOns(newOn().setTable1(table1).setName1(name1).setTable2(table2).setName2(name2)));
	}
	
	public Select joinAs(String table1, String alias, String name1, String table2, String name2)
	{
		return join(newJoin().setJoinType(JOIN).setTable(table1).setAlias(alias).addOns(newOn().setTable1(alias).setName1(name1).setTable2(table2).setName2(name2)));
	}
	
	public Select joinIndex(String table1, String index, String name1, String table2, String name2)
	{
		return join(newJoin().setJoinType(JOIN).setTable(table1).setIndex(String.format(USE_INDEX, index)).addOns(newOn().setTable1(table1).setName1(name1).setTable2(table2).setName2(name2)));
	}

	public Select joinAs(Select select, String alias, On...ons)
	{
		return join(newJoin().setJoinType(JOIN).setSelect(select).setAlias(alias).addOns(ons));
	}
	
	public Select joinAs(Select select, String alias, String name1, String table2, String name2)
	{
		return join(newJoin().setJoinType(JOIN).setSelect(select).setAlias(alias).addOns(newOn().setTable1(alias).setName1(name1).setTable2(table2).setName2(name2)));
	}
	
	public Select join(String table, On...ons)
	{
		return join(newJoin().setJoinType(JOIN).setTable(table).addOns(ons));
	}
	
	public Select joinAs(String table, String alias, On...ons)
	{
		return join(newJoin().setJoinType(JOIN).setTable(table).setAlias(alias).addOns(ons));
	}
	
	public Select joinInner(String table1, String name1, String table2, String name2)
	{
		return join(newJoin().setJoinType(INNER_JOIN).setTable(table1).addOns(newOn().setTable1(table1).setName1(name1).setTable2(table2).setName2(name2)));
	}

	public Select joinInnerIndex(String table1, String index, String name1, String table2, String name2)
	{
		return join(newJoin().setJoinType(INNER_JOIN).setTable(table1).setIndex(String.format(USE_INDEX, index)).addOns(newOn().setTable1(table1).setName1(name1).setTable2(table2).setName2(name2)));
	}

	public Select joinInnerAs(String table1, String alias, String name1, String table2, String name2)
	{
		return join(newJoin().setJoinType(INNER_JOIN).setTable(table1).setAlias(alias).addOns(newOn().setTable1(alias).setName1(name1).setTable2(table2).setName2(name2)));
	}
	
	public Select joinInnerAs(Select select, String alias, On...ons)
	{
		return join(newJoin().setJoinType(INNER_JOIN).setSelect(select).setAlias(alias).addOns(ons));
	}
	
	public Select joinInnerAs(Select select, String alias, String name1, String table2, String name2)
	{
		return join(newJoin().setJoinType(INNER_JOIN).setSelect(select).setAlias(alias).addOns(newOn().setTable1(alias).setName1(name1).setTable2(table2).setName2(name2)));
	}
	
	public Select joinInner(String table, On...ons)
	{
		return join(newJoin().setJoinType(INNER_JOIN).setTable(table).addOns(ons));
	}
	
	public Select joinInnerAs(String table, String alias, On...ons)
	{
		return join(newJoin().setJoinType(INNER_JOIN).setTable(table).setAlias(alias).addOns(ons));
	}
	
	public Select joinLeft(String table1, String name1, String table2, String name2)
	{
		return join(newJoin().setJoinType(LEFT_JOIN).setTable(table1).addOns(newOn().setTable1(table1).setName1(name1).setTable2(table2).setName2(name2)));
	}
	
	public Select joinLeftAs(String table1, String alias, String name1, String table2, String name2)
	{
		return join(newJoin().setJoinType(LEFT_JOIN).setTable(table1).setAlias(alias).addOns(newOn().setTable1(alias).setName1(name1).setTable2(table2).setName2(name2)));
	}
	
	public Select joinLeftIndex(String table1, String index, String name1, String table2, String name2)
	{
		return join(newJoin().setJoinType(LEFT_JOIN).setTable(table1).setIndex(String.format(USE_INDEX, index)).addOns(newOn().setTable1(table1).setName1(name1).setTable2(table2).setName2(name2)));
	}

	public Select joinLeftAs(Select select, String alias, On...ons)
	{
		return join(newJoin().setJoinType(LEFT_JOIN).setSelect(select).setAlias(alias).addOns(ons));
	}
	
	public Select joinLeftAs(Select select, String alias, String name1, String table2, String name2)
	{
		return join(newJoin().setJoinType(LEFT_JOIN).setSelect(select).setAlias(alias).addOns(newOn().setTable1(alias).setName1(name1).setTable2(table2).setName2(name2)));
	}
	
	public Select joinLeft(String table, On...ons)
	{
		return join(newJoin().setJoinType(LEFT_JOIN).setTable(table).addOns(ons));
	}
	
	public Select joinLeftAs(String table, String alias, On...ons)
	{
		return join(newJoin().setJoinType(LEFT_JOIN).setTable(table).setAlias(alias).addOns(ons));
	}
	
	public Select joinRight(String table1, String name1, String table2, String name2)
	{
		return join(newJoin().setJoinType(RIGHT_JOIN).setTable(table1).addOns(newOn().setTable1(table1).setName1(name1).setTable2(table2).setName2(name2)));
	}

	public Select joinRightIndex(String table1, String index, String name1, String table2, String name2)
	{
		return join(newJoin().setJoinType(RIGHT_JOIN).setTable(table1).setIndex(String.format(USE_INDEX, index)).addOns(newOn().setTable1(table1).setName1(name1).setTable2(table2).setName2(name2)));
	}

	public Select joinRightAs(String table1, String alias, String name1, String table2, String name2)
	{
		return join(newJoin().setJoinType(RIGHT_JOIN).setTable(table1).setAlias(alias).addOns(newOn().setTable1(alias).setName1(name1).setTable2(table2).setName2(name2)));
	}
	
	public Select joinRightAs(Select select, String alias, On...ons)
	{
		return join(newJoin().setJoinType(RIGHT_JOIN).setSelect(select).setAlias(alias).addOns(ons));
	}
	
	public Select joinRightAs(Select select, String alias, String name1, String table2, String name2)
	{
		return join(newJoin().setJoinType(RIGHT_JOIN).setSelect(select).setAlias(alias).addOns(newOn().setTable1(alias).setName1(name1).setTable2(table2).setName2(name2)));
	}
	
	public Select joinRight(String table, On...ons)
	{
		return join(newJoin().setJoinType(RIGHT_JOIN).setTable(table).addOns(ons));
	}
	
	public Select joinRightAs(String table, String alias, On...ons)
	{
		return join(newJoin().setJoinType(RIGHT_JOIN).setTable(table).setAlias(alias).addOns(ons));
	}
	
	public Select joinOuter(String table1, String name1, String table2, String name2)
	{
		return join(newJoin().setJoinType(OUTER_JOIN).setTable(table1).addOns(newOn().setTable1(table1).setName1(name1).setTable2(table2).setName2(name2)));
	}
	
	public Select joinOuterAs(String table1, String alias, String name1, String table2, String name2)
	{
		return join(newJoin().setJoinType(OUTER_JOIN).setTable(table1).setAlias(alias).addOns(newOn().setTable1(alias).setName1(name1).setTable2(table2).setName2(name2)));
	}
	
	public Select joinOuterAs(Select select, String alias, On...ons)
	{
		return join(newJoin().setJoinType(OUTER_JOIN).setSelect(select).setAlias(alias).addOns(ons));
	}
	
	public Select joinOuterAs(Select select, String alias, String name1, String table2, String name2)
	{
		return join(newJoin().setJoinType(OUTER_JOIN).setSelect(select).setAlias(alias).addOns(newOn().setTable1(alias).setName1(name1).setTable2(table2).setName2(name2)));
	}
	
	public Select joinOuter(String table, On...ons)
	{
		return join(newJoin().setJoinType(OUTER_JOIN).setTable(table).addOns(ons));
	}
	
	public Select joinOuterAs(String table, String alias, On...ons)
	{
		return join(newJoin().setJoinType(OUTER_JOIN).setTable(table).setAlias(alias).addOns(ons));
	}
	
	public Select wheres(Wheres wheres)
	{
		this.wheres = wheres;
		nestedWheres.clear();
		nestedWheres.add(wheres);
		return this;
	}
	
	public Select where(Where where)
	{
		Wheres wheres = nestedWheres.peekLast();
		if(wheres == null)
		{
			wheres = newWheres();
			wheres(wheres);
		}
		wheres.andWhere(where);
		return this;
	}
	
	public Select whereOpenParen()
	{
		Wheres wheres = newWheres();
		Wheres lastWheres = nestedWheres.peekLast();
		if(lastWheres == null)
		{
			lastWheres = newWheres();
			wheres(lastWheres);
		}
		nestedWheres.add(wheres);
		lastWheres.andWheres(wheres);
		return this;
	}
	
	public Select whereCloseParen()
	{
		nestedWheres.pollLast();
		return this;
	}
	
	public Select whereSql(String sql)
	{
		return where(newWhere().setSql(sql));
	}
	
	public Select whereSql(String sql, Object...values)
	{
		return where(newWhere().setSql(sql).addValues(values));
	}
	
	public Select whereSql(String sql, Collection<?> values)
	{
		return where(newWhere().setSql(sql).setValues(values));
	}
	
	public Select whereEquals(String name, Object value)
	{
		return where(newWhere().setName(name).setOpType(OP_EQ).addValues(value));
	}
	
	public Select whereEquals(String table, String name, Object value)
	{
		return where(newWhere().setTable(table).setName(name).setOpType(OP_EQ).addValues(value));
	}
	
	public Select whereNotEquals(String name, Object value)
	{
		return where(newWhere().setName(name).setOpType(OP_NE).addValues(value));
	}
	
	public Select whereNotEquals(String table, String name, Object value)
	{
		return where(newWhere().setTable(table).setName(name).setOpType(OP_NE).addValues(value));
	}
	
	public Select whereLessThan(String name, Object value)
	{
		return where(newWhere().setName(name).setOpType(OP_LT).addValues(value));
	}
	
	public Select whereLessThan(String table, String name, Object value)
	{
		return where(newWhere().setTable(table).setName(name).setOpType(OP_LT).addValues(value));
	}
	
	public Select whereGreaterThan(String name, Object value)
	{
		return where(newWhere().setName(name).setOpType(OP_GT).addValues(value));
	}
	
	public Select whereGreaterThan(String table, String name, Object value)
	{
		return where(newWhere().setTable(table).setName(name).setOpType(OP_GT).addValues(value));
	}
	
	public Select whereLessThanOrEquals(String name, Object value)
	{
		return where(newWhere().setName(name).setOpType(OP_LE).addValues(value));
	}
	
	public Select whereLessThanOrEquals(String table, String name, Object value)
	{
		return where(newWhere().setTable(table).setName(name).setOpType(OP_LE).addValues(value));
	}
	
	public Select whereGreaterThanOrEquals(String name, Object value)
	{
		return where(newWhere().setName(name).setOpType(OP_GE).addValues(value));
	}
	
	public Select whereGreaterThanOrEquals(String table, String name, Object value)
	{
		return where(newWhere().setTable(table).setName(name).setOpType(OP_GE).addValues(value));
	}
	
	public Select whereIn(String name, Object...values)
	{
		return where(newWhere().setName(name).setOpType(String.format(OP_IN, param(values.length))).addValues(values));
	}
	
	public Select whereIn(String name, Collection<?> values)
	{
		return where(newWhere().setName(name).setOpType(String.format(OP_IN, param(values.size()))).setValues(values));
	}
	
	public Select whereIn(String table, String name, Object...values)
	{
		return where(newWhere().setTable(table).setName(name).setOpType(String.format(OP_IN, param(values.length))).addValues(values));
	}
	
	public Select whereIn(String table, String name, Collection<?> values)
	{
		return where(newWhere().setTable(table).setName(name).setOpType(String.format(OP_IN, param(values.size()))).setValues(values));
	}
	
	public Select whereNotIn(String name, Object...values)
	{
		return where(newWhere().setName(name).setOpType(String.format(OP_NOT_IN, param(values.length))).addValues(values));
	}
	
	public Select whereNotIn(String name, Collection<?> values)
	{
		return where(newWhere().setName(name).setOpType(String.format(OP_NOT_IN, param(values.size()))).setValues(values));
	}
	
	public Select whereNotIn(String table, String name, Object...values)
	{
		return where(newWhere().setTable(table).setName(name).setOpType(String.format(OP_NOT_IN, param(values.length))).addValues(values));
	}
	
	public Select whereNotIn(String table, String name, Collection<?> values)
	{
		return where(newWhere().setTable(table).setName(name).setOpType(String.format(OP_NOT_IN, param(values.size()))).setValues(values));
	}
	
	public Select whereLike(String name, Object value)
	{
		return where(newWhere().setName(name).setOpType(OP_LIKE).addValues(value));
	}
	
	public Select whereLike(String table, String name, Object value)
	{
		return where(newWhere().setTable(table).setName(name).setOpType(OP_LIKE).addValues(value));
	}
	
	public Select whereNotLike(String name, Object value)
	{
		return where(newWhere().setName(name).setOpType(OP_NOT_LIKE).addValues(value));
	}
	
	public Select whereNotLike(String table, String name, Object value)
	{
		return where(newWhere().setTable(table).setName(name).setOpType(OP_NOT_LIKE).addValues(value));
	}
	
	public Select whereBetween(String name, Object value1, Object value2)
	{
		return where(newWhere().setName(name).setOpType(OP_BETWEEN).addValues(value1, value2));
	}
	
	public Select whereBetween(String table, String name, Object value1, Object value2)
	{
		return where(newWhere().setTable(table).setName(name).setOpType(OP_BETWEEN).addValues(value1, value2));
	}
	
	public Select whereIsNull(String name)
	{
		return where(newWhere().setName(name).setOpType(OP_IS_NULL));
	}
	
	public Select whereIsNull(String table, String name)
	{
		return where(newWhere().setTable(table).setName(name).setOpType(OP_IS_NULL));
	}
	
	public Select whereIsNotNull(String name)
	{
		return where(newWhere().setName(name).setOpType(OP_IS_NOT_NULL));
	}
	
	public Select whereIsNotNull(String table, String name)
	{
		return where(newWhere().setTable(table).setName(name).setOpType(OP_IS_NOT_NULL));
	}
	
	public Select whereExists(Select select)
	{
		return where(newWhere().setSql(EXISTS_SQL).setSelect(select));
	}
	
	public Select whereNotExists(Select select)
	{
		return where(newWhere().setSql(NOT_EXISTS_SQL).setSelect(select));
	}
	
	public Select orWhere(Where where)
	{
		Wheres wheres = nestedWheres.peekLast();
		if(wheres == null)
		{
			wheres = newWheres();
			wheres(wheres);
		}
		wheres.orWhere(where);
		return this;
	}
	
	public Select orWhereOpenParen()
	{
		Wheres wheres = newWheres();
		Wheres lastWheres = nestedWheres.peekLast();
		if(lastWheres == null)
		{
			lastWheres = newWheres();
			wheres(lastWheres);
		}
		nestedWheres.add(wheres);
		lastWheres.orWheres(wheres);
		return this;
	}
	
	public Select orWhereSql(String sql)
	{
		return orWhere(newWhere().setSql(sql));
	}
	
	public Select orWhereSql(String sql, Object...values)
	{
		return orWhere(newWhere().setSql(sql).addValues(values));
	}
	
	public Select orWhereSql(String sql, Collection<?> values)
	{
		return orWhere(newWhere().setSql(sql).setValues(values));
	}
	
	public Select orWhereEquals(String name, Object value)
	{
		return orWhere(newWhere().setName(name).setOpType(OP_EQ).addValues(value));
	}
	
	public Select orWhereEquals(String table, String name, Object value)
	{
		return orWhere(newWhere().setTable(table).setName(name).setOpType(OP_EQ).addValues(value));
	}
	
	public Select orWhereNotEquals(String name, Object value)
	{
		return orWhere(newWhere().setName(name).setOpType(OP_NE).addValues(value));
	}
	
	public Select orWhereNotEquals(String table, String name, Object value)
	{
		return orWhere(newWhere().setTable(table).setName(name).setOpType(OP_NE).addValues(value));
	}
	
	public Select orWhereLessThan(String name, Object value)
	{
		return orWhere(newWhere().setName(name).setOpType(OP_LT).addValues(value));
	}
	
	public Select orWhereLessThan(String table, String name, Object value)
	{
		return orWhere(newWhere().setTable(table).setName(name).setOpType(OP_LT).addValues(value));
	}
	
	public Select orWhereGreaterThan(String name, Object value)
	{
		return orWhere(newWhere().setName(name).setOpType(OP_GT).addValues(value));
	}
	
	public Select orWhereGreaterThan(String table, String name, Object value)
	{
		return orWhere(newWhere().setTable(table).setName(name).setOpType(OP_GT).addValues(value));
	}
	
	public Select orWhereLessThanOrEquals(String name, Object value)
	{
		return orWhere(newWhere().setName(name).setOpType(OP_LE).addValues(value));
	}
	
	public Select orWhereLessThanOrEquals(String table, String name, Object value)
	{
		return orWhere(newWhere().setTable(table).setName(name).setOpType(OP_LE).addValues(value));
	}
	
	public Select orWhereGreaterThanOrEquals(String name, Object value)
	{
		return orWhere(newWhere().setName(name).setOpType(OP_GE).addValues(value));
	}
	
	public Select orWhereGreaterThanOrEquals(String table, String name, Object value)
	{
		return orWhere(newWhere().setTable(table).setName(name).setOpType(OP_GE).addValues(value));
	}
	
	public Select orWhereIn(String name, Object...values)
	{
		return orWhere(newWhere().setName(name).setOpType(String.format(OP_IN, param(values.length))).addValues(values));
	}
	
	public Select orWhereIn(String name, Collection<?> values)
	{
		return orWhere(newWhere().setName(name).setOpType(String.format(OP_IN, param(values.size()))).setValues(values));
	}
	
	public Select orWhereIn(String table, String name, Object...values)
	{
		return orWhere(newWhere().setTable(table).setName(name).setOpType(String.format(OP_IN, param(values.length))).addValues(values));
	}
	
	public Select orWhereIn(String table, String name, Collection<?> values)
	{
		return orWhere(newWhere().setTable(table).setName(name).setOpType(String.format(OP_IN, param(values.size()))).setValues(values));
	}
	
	public Select orWhereNotIn(String name, Object...values)
	{
		return orWhere(newWhere().setName(name).setOpType(String.format(OP_NOT_IN, param(values.length))).addValues(values));
	}
	
	public Select orWhereNotIn(String name, Collection<?> values)
	{
		return orWhere(newWhere().setName(name).setOpType(String.format(OP_NOT_IN, param(values.size()))).setValues(values));
	}
	
	public Select orWhereNotIn(String table, String name, Object...values)
	{
		return orWhere(newWhere().setTable(table).setName(name).setOpType(String.format(OP_NOT_IN, param(values.length))).addValues(values));
	}
	
	public Select orWhereNotIn(String table, String name, Collection<?> values)
	{
		return orWhere(newWhere().setTable(table).setName(name).setOpType(String.format(OP_NOT_IN, param(values.size()))).setValues(values));
	}
	
	public Select orWhereLike(String name, Object value)
	{
		return orWhere(newWhere().setName(name).setOpType(OP_LIKE).addValues(value));
	}
	
	public Select orWhereLike(String table, String name, Object value)
	{
		return orWhere(newWhere().setTable(table).setName(name).setOpType(OP_LIKE).addValues(value));
	}
	
	public Select orWhereNotLike(String name, Object value)
	{
		return orWhere(newWhere().setName(name).setOpType(OP_NOT_LIKE).addValues(value));
	}
	
	public Select orWhereNotLike(String table, String name, Object value)
	{
		return orWhere(newWhere().setTable(table).setName(name).setOpType(OP_NOT_LIKE).addValues(value));
	}
	
	public Select orWhereBetween(String name, Object value1, Object value2)
	{
		return orWhere(newWhere().setName(name).setOpType(OP_BETWEEN).addValues(value1, value2));
	}
	
	public Select orWhereBetween(String table, String name, Object value1, Object value2)
	{
		return orWhere(newWhere().setTable(table).setName(name).setOpType(OP_BETWEEN).addValues(value1, value2));
	}
	
	public Select orWhereIsNull(String name)
	{
		return orWhere(newWhere().setName(name).setOpType(OP_IS_NULL));
	}
	
	public Select orWhereIsNull(String table, String name)
	{
		return orWhere(newWhere().setTable(table).setName(name).setOpType(OP_IS_NULL));
	}
	
	public Select orWhereIsNotNull(String name)
	{
		return orWhere(newWhere().setName(name).setOpType(OP_IS_NOT_NULL));
	}
	
	public Select orWhereIsNotNull(String table, String name)
	{
		return orWhere(newWhere().setTable(table).setName(name).setOpType(OP_IS_NOT_NULL));
	}
	
	public Select orWhereExists(Select select)
	{
		return orWhere(newWhere().setSql(EXISTS_SQL).setSelect(select));
	}
	
	public Select orWhereNotExists(Select select)
	{
		return orWhere(newWhere().setSql(NOT_EXISTS_SQL).setSelect(select));
	}
	
	public Select groups(Groups groups)
	{
		this.groups = groups;
		return this;
	}
	
	public Select group(Group group)
	{
		groups = groups != null ? groups : newGroups();
		groups.addGroups(group);
		return this;
	}
	
	public Select groupBy(String name)
	{
		return group(newGroup().setName(name));
	}
	
	public Select groupBy(String table, String name)
	{
		return group(newGroup().setTable(table).setName(name));
	}
	
	public Select groupBySql(String sql)
	{
		return group(newGroup().setSql(sql));
	}
	
	public Select havings(Havings havings)
	{
		this.havings = havings;
		nestedHavings.clear();
		nestedHavings.add(havings);
		return this;
	}
	
	public Select having(Having having)
	{
		Havings havings = nestedHavings.peekLast();
		if(havings == null)
		{
			havings = newHavings();
			havings(havings);
		}
		havings.andHaving(having);
		return this;
	}
	
	public Select havingOpenParen()
	{
		Havings havings = newHavings();
		Havings lastHavings = nestedHavings.peekLast();
		if(lastHavings == null)
		{
			lastHavings = newHavings();
			havings(lastHavings);
		}
		nestedHavings.add(havings);
		lastHavings.andHavings(havings);
		return this;
	}
	
	public Select havingCloseParen()
	{
		nestedHavings.pollLast();
		return this;
	}
	
	public Select havingSql(String sql)
	{
		return having(newHaving().setSql(sql));
	}
	
	public Select havingSql(String sql, Object...values)
	{
		return having(newHaving().setSql(sql).addValues(values));
	}
	
	public Select havingSql(String sql, Collection<?> values)
	{
		return having(newHaving().setSql(sql).setValues(values));
	}
	
	public Select havingEquals(String name, Object value)
	{
		return having(newHaving().setName(name).setOpType(OP_EQ).addValues(value));
	}
	
	public Select havingEquals(String table, String name, Object value)
	{
		return having(newHaving().setTable(table).setName(name).setOpType(OP_EQ).addValues(value));
	}
	
	public Select havingNotEquals(String name, Object value)
	{
		return having(newHaving().setName(name).setOpType(OP_NE).addValues(value));
	}
	
	public Select havingNotEquals(String table, String name, Object value)
	{
		return having(newHaving().setTable(table).setName(name).setOpType(OP_NE).addValues(value));
	}
	
	public Select havingLessThan(String name, Object value)
	{
		return having(newHaving().setName(name).setOpType(OP_LT).addValues(value));
	}
	
	public Select havingLessThan(String table, String name, Object value)
	{
		return having(newHaving().setTable(table).setName(name).setOpType(OP_LT).addValues(value));
	}
	
	public Select havingGreaterThan(String name, Object value)
	{
		return having(newHaving().setName(name).setOpType(OP_GT).addValues(value));
	}
	
	public Select havingGreaterThan(String table, String name, Object value)
	{
		return having(newHaving().setTable(table).setName(name).setOpType(OP_GT).addValues(value));
	}
	
	public Select havingLessThanOrEquals(String name, Object value)
	{
		return having(newHaving().setName(name).setOpType(OP_LE).addValues(value));
	}
	
	public Select havingLessThanOrEquals(String table, String name, Object value)
	{
		return having(newHaving().setTable(table).setName(name).setOpType(OP_LE).addValues(value));
	}
	
	public Select havingGreaterThanOrEquals(String name, Object value)
	{
		return having(newHaving().setName(name).setOpType(OP_GE).addValues(value));
	}
	
	public Select havingGreaterThanOrEquals(String table, String name, Object value)
	{
		return having(newHaving().setTable(table).setName(name).setOpType(OP_GE).addValues(value));
	}
	
	public Select havingIn(String name, Object...values)
	{
		return having(newHaving().setName(name).setOpType(String.format(OP_IN, param(values.length))).addValues(values));
	}
	
	public Select havingIn(String name, Collection<?> values)
	{
		return having(newHaving().setName(name).setOpType(String.format(OP_IN, param(values.size()))).setValues(values));
	}
	
	public Select havingIn(String table, String name, Object...values)
	{
		return having(newHaving().setTable(table).setName(name).setOpType(String.format(OP_IN, param(values.length))).addValues(values));
	}
	
	public Select havingIn(String table, String name, Collection<?> values)
	{
		return having(newHaving().setTable(table).setName(name).setOpType(String.format(OP_IN, param(values.size()))).setValues(values));
	}
	
	public Select havingNotIn(String name, Object...values)
	{
		return having(newHaving().setName(name).setOpType(String.format(OP_NOT_IN, param(values.length))).addValues(values));
	}
	
	public Select havingNotIn(String name, Collection<?> values)
	{
		return having(newHaving().setName(name).setOpType(String.format(OP_NOT_IN, param(values.size()))).setValues(values));
	}
	
	public Select havingNotIn(String table, String name, Object...values)
	{
		return having(newHaving().setTable(table).setName(name).setOpType(String.format(OP_NOT_IN, param(values.length))).addValues(values));
	}
	
	public Select havingNotIn(String table, String name, Collection<?> values)
	{
		return having(newHaving().setTable(table).setName(name).setOpType(String.format(OP_NOT_IN, param(values.size()))).setValues(values));
	}
	
	public Select havingLike(String name, Object value)
	{
		return having(newHaving().setName(name).setOpType(OP_LIKE).addValues(value));
	}
	
	public Select havingLike(String table, String name, Object value)
	{
		return having(newHaving().setTable(table).setName(name).setOpType(OP_LIKE).addValues(value));
	}
	
	public Select havingNotLike(String name, Object value)
	{
		return having(newHaving().setName(name).setOpType(OP_NOT_LIKE).addValues(value));
	}
	
	public Select havingNotLike(String table, String name, Object value)
	{
		return having(newHaving().setTable(table).setName(name).setOpType(OP_NOT_LIKE).addValues(value));
	}
	
	public Select havingBetween(String name, Object value1, Object value2)
	{
		return having(newHaving().setName(name).setOpType(OP_BETWEEN).addValues(value1, value2));
	}
	
	public Select havingBetween(String table, String name, Object value1, Object value2)
	{
		return having(newHaving().setTable(table).setName(name).setOpType(OP_BETWEEN).addValues(value1, value2));
	}
	
	public Select havingIsNull(String name)
	{
		return having(newHaving().setName(name).setOpType(OP_IS_NULL));
	}
	
	public Select havingIsNull(String table, String name)
	{
		return having(newHaving().setTable(table).setName(name).setOpType(OP_IS_NULL));
	}
	
	public Select havingIsNotNull(String name)
	{
		return having(newHaving().setName(name).setOpType(OP_IS_NOT_NULL));
	}
	
	public Select havingIsNotNull(String table, String name)
	{
		return having(newHaving().setTable(table).setName(name).setOpType(OP_IS_NOT_NULL));
	}
	
	public Select orHaving(Having having)
	{
		Havings havings = nestedHavings.peekLast();
		if(havings == null)
		{
			havings = newHavings();
			havings(havings);
		}
		havings.orHaving(having);
		return this;
	}
	
	public Select orHavingOpenParen()
	{
		Havings havings = newHavings();
		Havings lastHavings = nestedHavings.peekLast();
		if(lastHavings == null)
		{
			lastHavings = newHavings();
			havings(lastHavings);
		}
		nestedHavings.add(havings);
		lastHavings.orHavings(havings);
		return this;
	}
	
	public Select orHavingSql(String sql)
	{
		return orHaving(newHaving().setSql(sql));
	}
	
	public Select orHavingSql(String sql, Object...values)
	{
		return orHaving(newHaving().setSql(sql).addValues(values));
	}
	
	public Select orHavingSql(String sql, Collection<?> values)
	{
		return orHaving(newHaving().setSql(sql).setValues(values));
	}
	
	public Select orHavingEquals(String name, Object value)
	{
		return orHaving(newHaving().setName(name).setOpType(OP_EQ).addValues(value));
	}
	
	public Select orHavingEquals(String table, String name, Object value)
	{
		return orHaving(newHaving().setTable(table).setName(name).setOpType(OP_EQ).addValues(value));
	}
	
	public Select orHavingNotEquals(String name, Object value)
	{
		return orHaving(newHaving().setName(name).setOpType(OP_NE).addValues(value));
	}
	
	public Select orHavingNotEquals(String table, String name, Object value)
	{
		return orHaving(newHaving().setTable(table).setName(name).setOpType(OP_NE).addValues(value));
	}
	
	public Select orHavingLessThan(String name, Object value)
	{
		return orHaving(newHaving().setName(name).setOpType(OP_LT).addValues(value));
	}
	
	public Select orHavingLessThan(String table, String name, Object value)
	{
		return orHaving(newHaving().setTable(table).setName(name).setOpType(OP_LT).addValues(value));
	}
	
	public Select orHavingGreaterThan(String name, Object value)
	{
		return orHaving(newHaving().setName(name).setOpType(OP_GT).addValues(value));
	}
	
	public Select orHavingGreaterThan(String table, String name, Object value)
	{
		return orHaving(newHaving().setTable(table).setName(name).setOpType(OP_GT).addValues(value));
	}
	
	public Select orHavingLessThanOrEquals(String name, Object value)
	{
		return orHaving(newHaving().setName(name).setOpType(OP_LE).addValues(value));
	}
	
	public Select orHavingLessThanOrEquals(String table, String name, Object value)
	{
		return orHaving(newHaving().setTable(table).setName(name).setOpType(OP_LE).addValues(value));
	}
	
	public Select orHavingGreaterThanOrEquals(String name, Object value)
	{
		return orHaving(newHaving().setName(name).setOpType(OP_GE).addValues(value));
	}
	
	public Select orHavingGreaterThanOrEquals(String table, String name, Object value)
	{
		return orHaving(newHaving().setTable(table).setName(name).setOpType(OP_GE).addValues(value));
	}
	
	public Select orHavingIn(String name, Object...values)
	{
		return orHaving(newHaving().setName(name).setOpType(String.format(OP_IN, param(values.length))).addValues(values));
	}
	
	public Select orHavingIn(String name, Collection<?> values)
	{
		return orHaving(newHaving().setName(name).setOpType(String.format(OP_IN, param(values.size()))).setValues(values));
	}
	
	public Select orHavingIn(String table, String name, Object...values)
	{
		return orHaving(newHaving().setTable(table).setName(name).setOpType(String.format(OP_IN, param(values.length))).addValues(values));
	}
	
	public Select orHavingIn(String table, String name, Collection<?> values)
	{
		return orHaving(newHaving().setTable(table).setName(name).setOpType(String.format(OP_IN, param(values.size()))).setValues(values));
	}
	
	public Select orHavingNotIn(String name, Object...values)
	{
		return orHaving(newHaving().setName(name).setOpType(String.format(OP_NOT_IN, param(values.length))).addValues(values));
	}
	
	public Select orHavingNotIn(String name, Collection<?> values)
	{
		return orHaving(newHaving().setName(name).setOpType(String.format(OP_NOT_IN, param(values.size()))).setValues(values));
	}
	
	public Select orHavingNotIn(String table, String name, Object...values)
	{
		return orHaving(newHaving().setTable(table).setName(name).setOpType(String.format(OP_NOT_IN, param(values.length))).addValues(values));
	}
	
	public Select orHavingNotIn(String table, String name, Collection<?> values)
	{
		return orHaving(newHaving().setTable(table).setName(name).setOpType(String.format(OP_NOT_IN, param(values.size()))).setValues(values));
	}
	
	public Select orHavingLike(String name, Object value)
	{
		return orHaving(newHaving().setName(name).setOpType(OP_LIKE).addValues(value));
	}
	
	public Select orHavingLike(String table, String name, Object value)
	{
		return orHaving(newHaving().setTable(table).setName(name).setOpType(OP_LIKE).addValues(value));
	}
	
	public Select orHavingNotLike(String name, Object value)
	{
		return orHaving(newHaving().setName(name).setOpType(OP_NOT_LIKE).addValues(value));
	}
	
	public Select orHavingNotLike(String table, String name, Object value)
	{
		return orHaving(newHaving().setTable(table).setName(name).setOpType(OP_NOT_LIKE).addValues(value));
	}
	
	public Select orHavingBetween(String name, Object value1, Object value2)
	{
		return orHaving(newHaving().setName(name).setOpType(OP_BETWEEN).addValues(value1, value2));
	}
	
	public Select orHavingBetween(String table, String name, Object value1, Object value2)
	{
		return orHaving(newHaving().setTable(table).setName(name).setOpType(OP_BETWEEN).addValues(value1, value2));
	}
	
	public Select orHavingIsNull(String name)
	{
		return orHaving(newHaving().setName(name).setOpType(OP_IS_NULL));
	}
	
	public Select orHavingIsNull(String table, String name)
	{
		return orHaving(newHaving().setTable(table).setName(name).setOpType(OP_IS_NULL));
	}
	
	public Select orHavingIsNotNull(String name)
	{
		return orHaving(newHaving().setName(name).setOpType(OP_IS_NOT_NULL));
	}
	
	public Select orHavingIsNotNull(String table, String name)
	{
		return orHaving(newHaving().setTable(table).setName(name).setOpType(OP_IS_NOT_NULL));
	}
	
	public Select orders(Orders orders)
	{
		this.orders = orders;
		return this;
	}
	
	public Select order(Order order)
	{
		orders = orders != null ? orders : newOrders();
		orders.addOrders(order);
		return this;
	}
	
	public Select orderByAsc(String name)
	{
		return order(newOrder().setName(name).setOrderType(ASC));
	}
	
	public Select orderByAsc(String table, String name)
	{
		return order(newOrder().setTable(table).setName(name).setOrderType(ASC));
	}
	
	public Select orderByDesc(String name)
	{
		return order(newOrder().setName(name).setOrderType(DESC));
	}
	
	public Select orderByDesc(String table, String name)
	{
		return order(newOrder().setTable(table).setName(name).setOrderType(DESC));
	}
	
	public Select orderBy(String sql)
	{
		return order(newOrder().setSql(sql));
	}
	
	public Select page(Page page)
	{
		this.page = page;
		return this;
	}
	
	public Select limit(int limit)
	{
		return page(newPage().setLimit(limit));
	}
	
	public Select offset(int offset)
	{
		return page(newPage().setOffset(offset));
	}
	
	public Select limitOffset(int limit, int offset)
	{
		return page(newPage().setLimit(limit).setOffset(offset));
	}
	
	@Override
	public List<Object> getValues()
	{
		List<Object> values = new List<Object>().allowNull();
		if(from != null)
		{
			values.addAll(from.getValues());
		}
		if(columns != null)
		{
			values.addAll(columns.getValues());
		}
		if(joins != null)
		{
			values.addAll(joins.getValues());
		}
		if(wheres != null)
		{
			values.addAll(wheres.getValues());
		}
		if(havings != null)
		{
			values.addAll(havings.getValues());
		}
		return values;
	}
	
	@Override
	public String toString()
	{
		return toString(true);
	}
	
	public String toString(boolean end)
	{
		if(columns == null)
		{
			columnAll(from.alias());
		}
		StringBuilder builder = new StringBuilder();
		builder.append(SELECT);
		if(distinct)
		{
			builder.append(DISTINCT);
		}
		builder.append(columns);
		builder.append(from != null ? from : "");
		builder.append(joins != null ? joins : "");
		builder.append(wheres != null ? wheres : "");
		builder.append(groups != null ? groups : "");
		builder.append(havings != null ? havings : "");
		builder.append(orders != null ? orders : "");
		builder.append(page != null ? page : "");
		builder.append(end ? END : "");
		return builder.toString();
	}
	
}
