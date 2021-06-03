package com.example.sudoku.game

class Cell(
    val row: Int,
    val col: Int,
    var value: Int,
    var isStartingCell: Boolean = false,                //Cac cell duoc khoi tao ban dau va co san value, khong the thay doi
    var isRightValue: Boolean = false                   //So sanh input value cua cell voi cell tuong ung cua solution, xac dinh gia tri dung cua cell
)