package com.example.sudoku.game

class Board(var size: Int, var cells: List<Cell>) {
    fun getCell(row: Int, col: Int) = cells[row * size + col]
}