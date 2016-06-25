// Generated from com/caseystella/parser/Date.g4 by ANTLR 4.3
package com.caseystella.parser;
import org.antlr.v4.runtime.misc.NotNull;
import org.antlr.v4.runtime.tree.ParseTreeListener;

/**
 * This interface defines a complete listener for a parse tree produced by
 * {@link DateParser}.
 */
public interface DateListener extends ParseTreeListener {
	/**
	 * Enter a parse tree produced by {@link DateParser#date}.
	 * @param ctx the parse tree
	 */
	void enterDate(@NotNull DateParser.DateContext ctx);
	/**
	 * Exit a parse tree produced by {@link DateParser#date}.
	 * @param ctx the parse tree
	 */
	void exitDate(@NotNull DateParser.DateContext ctx);

	/**
	 * Enter a parse tree produced by {@link DateParser#month_digit}.
	 * @param ctx the parse tree
	 */
	void enterMonth_digit(@NotNull DateParser.Month_digitContext ctx);
	/**
	 * Exit a parse tree produced by {@link DateParser#month_digit}.
	 * @param ctx the parse tree
	 */
	void exitMonth_digit(@NotNull DateParser.Month_digitContext ctx);

	/**
	 * Enter a parse tree produced by {@link DateParser#month_text}.
	 * @param ctx the parse tree
	 */
	void enterMonth_text(@NotNull DateParser.Month_textContext ctx);
	/**
	 * Exit a parse tree produced by {@link DateParser#month_text}.
	 * @param ctx the parse tree
	 */
	void exitMonth_text(@NotNull DateParser.Month_textContext ctx);

	/**
	 * Enter a parse tree produced by {@link DateParser#month}.
	 * @param ctx the parse tree
	 */
	void enterMonth(@NotNull DateParser.MonthContext ctx);
	/**
	 * Exit a parse tree produced by {@link DateParser#month}.
	 * @param ctx the parse tree
	 */
	void exitMonth(@NotNull DateParser.MonthContext ctx);

	/**
	 * Enter a parse tree produced by {@link DateParser#year}.
	 * @param ctx the parse tree
	 */
	void enterYear(@NotNull DateParser.YearContext ctx);
	/**
	 * Exit a parse tree produced by {@link DateParser#year}.
	 * @param ctx the parse tree
	 */
	void exitYear(@NotNull DateParser.YearContext ctx);

	/**
	 * Enter a parse tree produced by {@link DateParser#day}.
	 * @param ctx the parse tree
	 */
	void enterDay(@NotNull DateParser.DayContext ctx);
	/**
	 * Exit a parse tree produced by {@link DateParser#day}.
	 * @param ctx the parse tree
	 */
	void exitDay(@NotNull DateParser.DayContext ctx);

	/**
	 * Enter a parse tree produced by {@link DateParser#separator}.
	 * @param ctx the parse tree
	 */
	void enterSeparator(@NotNull DateParser.SeparatorContext ctx);
	/**
	 * Exit a parse tree produced by {@link DateParser#separator}.
	 * @param ctx the parse tree
	 */
	void exitSeparator(@NotNull DateParser.SeparatorContext ctx);

	/**
	 * Enter a parse tree produced by {@link DateParser#day_of_week}.
	 * @param ctx the parse tree
	 */
	void enterDay_of_week(@NotNull DateParser.Day_of_weekContext ctx);
	/**
	 * Exit a parse tree produced by {@link DateParser#day_of_week}.
	 * @param ctx the parse tree
	 */
	void exitDay_of_week(@NotNull DateParser.Day_of_weekContext ctx);
}