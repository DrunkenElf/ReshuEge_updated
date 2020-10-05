package com.ilnur

import android.util.Log

import org.json.JSONObject

import java.util.HashMap


class FirstPoint(subj: String) {
    //String[] pointsCount;
    var pluses = ""
        private set
    private var answerState: Boolean = false
    var color: String? = null
        private set
    private var subj_data: JSONObject? = null

    init {

        try {
            subj_data = SubjInfo()._subj_data!!.getJSONObject(subj)
            Log.d("myLogs", subj_data!!.toString())
        } catch (e: Exception) {
            Log.d("myLogs", "subjinfo problem")
            //Log.d("myLogs", e.message)
        }

    }

    fun getFirtsPoint(answer: String, rightAnswer: String, questionNumber: Int, category: Int): String {
        var answer = answer
        var rightAnswer = rightAnswer
        var max: Int? = 1
        var mode: Int? = 0
        Log.d("ANSWERCHECK", "ans check")
        Log.d("SUBJ_data", subj_data!!.toString())
        Log.d("MY_ANSWER", answer)
        Log.d("RIGHT_ANSWER", rightAnswer)
        Log.d("QUES_NUM", " $questionNumber")
        Log.d("CATEGORY", " $category")
        try {
            max = subj_data!!.getJSONArray("tasks").getJSONObject(questionNumber).getInt("max")//  Integer.parseInt(pointMax)
            //mode = subj_data.getJSONObject("rules").getInt(String.valueOf(category));
            mode = Integer.parseInt(subj_data!!.getJSONObject("rules").getString(category.toString()))
        } catch (e: Exception) {
            Log.d("myLogs", e.toString())
            max = 1
            mode = 0
        }
        //280 355 211 352 175-3
        Log.d("MAX_MODE", "check - " + max!!.toString() + " " + mode!!.toString())
        /*if (mode % 2 == 0) {
            Log.d("ToLowerCase", "TRT")
            answer = answer.toLowerCase()
            rightAnswer = rightAnswer.toLowerCase()
        }*/
        var mindist = Integer.MAX_VALUE
        Log.d("MINDIST", mindist.toString() + "")
        var dist: Int
        val rights = rightAnswer.split("\\|".toRegex()).dropLastWhile({ it.isEmpty() }).toTypedArray()
        for (i in rights.indices) {
            if (mode / 2 == 1) {
                Log.d("MODE LEVE", "" + mode)
                dist = leven(rights[i], answer)
            } else if (mode / 2 == 2) {
                Log.d("MODE SOC", "" + mode)
                dist = soc_dist(rights[i], answer)
            } else if (mode / 2 == 3) {
                Log.d("MODE TWOG", "" + mode)
                dist = twog(rights[i], answer)
            } else {
                Log.d("MODE DAM", "" + mode)
                dist = damerau(rights[i], answer)
            }
            if (dist < mindist) {
                mindist = dist
            }
            Log.d("MIndist$i", "" + mindist)
            Log.d("DIST$i", "" + dist)
        }
        val currentPoint: Int
        if (mindist >= max) {
            currentPoint = 0
        } else {
            currentPoint = max - mindist
        }
        Log.d("MAX", "" + max)
        Log.d("MINDIST", "" + mindist)
        Log.d("CURRENTpoint", "" + currentPoint)

        if (currentPoint == max) {
            answerState = true
            color = "green"
            pluses += "+"
        } else if (currentPoint == 0) {
            answerState = false
            color = "red"
            pluses += "-"
        } else {
            answerState = false
            color = "yellow"
            pluses += "-"
        }
        return Integer.toString(currentPoint)

    }

    fun task_count(): Int {

        var max: Int
        try {
            max = subj_data!!.getJSONArray("tasks").length()//  Integer.parseInt(pointMax)
        } catch (e: Exception) {
            Log.d("myLogs", e.toString())
            max = 1
        }

        return max

    }

    fun get_max(questionNumber: Int): String {
        var max: Int
        try {
            max = subj_data!!.getJSONArray("tasks").getJSONObject(questionNumber - 1).getInt("max")//  Integer.parseInt(pointMax)
        } catch (e: Exception) {
            Log.d("myLogs", e.toString())
            max = 1
        }

        return Integer.toString(max)
    }
    /*public String performPoint(String pointString) {
        for (int i = 0; i < pointString.length(); i++) {
            if (pointString.charAt(i) == ',') {
                pointString = pointString.replace(pointString.substring(i, pointString.length()), "");
                break;
            }
        }
        return pointString;
    }*/

    /*String compareAnswers(String answer, String rightAnswer) {

        if (answer.length() < rightAnswer.length()) {
            for (int i = 0; i < rightAnswer.length() - answer.length(); i++) {
                answer += " ";
            }
        }

        return answer;
    }*/


    fun checkAnswer(): Boolean {
        return answerState
    }

    fun maxPoints(): Int {
        var max = 0

        try {
            val tasks = subj_data!!.getJSONArray("tasks")
            for (i in 0 until tasks.length()) {
                max += tasks.getJSONObject(i).getInt("max")
            }
        } catch (e: Exception) {
            Log.d("myLogs", e.toString())
            max = 0
        }

        return max
    }

    private fun leven(S1: String, S2: String): Int {
        val m = S1.length
        val n = S2.length
        var D1: IntArray
        var D2 = IntArray(n + 1)

        for (i in 0..n)
            D2[i] = i

        for (i in 1..m) {
            D1 = D2
            D2 = IntArray(n + 1)
            for (j in 0..n) {
                if (j == 0)
                    D2[j] = i
                else {
                    val cost = if (!compare(S1[i - 1].toString(),S2[j - 1].toString())) 1 else 0
                    if (D2[j - 1] < D1[j] && D2[j - 1] < D1[j - 1] + cost)
                        D2[j] = D2[j - 1] + 1
                    else if (D1[j] < D1[j - 1] + cost)
                        D2[j] = D1[j] + 1
                    else
                        D2[j] = D1[j - 1] + cost
                }
            }
        }
        return D2[n]
    }

    class Pair(val rus: String, val eng: String)

    var pairs = arrayOf(Pair("С","c"),Pair("С","C"),
            Pair("Е","e"),Pair("Е","E"),Pair("Т","T"),
            Pair("О","o"),Pair("О","O"),Pair("Р","p"),Pair("Р","P"),
            Pair("А","a"),Pair("А","A"),Pair("Н","H"),
            Pair("К","k"),Pair("Х","x"),Pair("К","K"),Pair("Х","X"),
            Pair("В","B"))
    fun compare(first: String, second: String): Boolean {
        //C E T O P A H K X B M
        val arr = "CETOPAHKXBM"
        var res = false
        /*pairs.forEach { pair ->
            run {
                if ((first == pair.rus && second == pair.eng) || (first == pair.eng && second == pair.rus)) {
                    //res = true
                    Log.d("compare", first+" / "+second)
                    return true
                }
            }
        }*/
        Log.d("compare", first+" / "+second)
        for (pair in pairs){
            if ((first == pair.rus && second == pair.eng) || (first == pair.eng && second == pair.rus)) {
                //res = true
                Log.d("compareInc", first+" / "+second)
                return true
            }
        }

        return first.equals(second, ignoreCase = true)
    }

    private fun damerau(source: String, target: String): Int {
        if (source.length == 0) {
            return target.length
        }
        if (target.length == 0) {
            return source.length
        }
        val table = Array(source.length) { IntArray(target.length) }
        val sourceIndexByCharacter = HashMap<Char, Int>()
        if (!compare(source[0].toString(), target[0].toString())) {
            table[0][0] = Math.min(1, 1 + 1)
        }
        sourceIndexByCharacter[source[0]] = 0
        for (i in 1 until source.length) {
            val deleteDistance = table[i - 1][0] + 1
            val insertDistance = i + 1 + 1
            val matchDistance = i + if (compare(source[i].toString(), target[0].toString())) 0 else 1
            table[i][0] = Math.min(Math.min(deleteDistance, insertDistance),
                    matchDistance)
        }
        for (j in 1 until target.length) {
            val deleteDistance = j + 1 + 1
            val insertDistance = table[0][j - 1] + 1
            val matchDistance = j + if (compare(source[0].toString(), target[0].toString())) 0 else 1
            table[0][j] = Math.min(Math.min(deleteDistance, insertDistance),
                    matchDistance)
        }
        for (i in 1 until source.length) {
            var maxSourceLetterMatchIndex = if (compare(source[i].toString(), target[0].toString()))
                0
            else
                -1
            for (j in 1 until target.length) {
                val candidateSwapIndex = sourceIndexByCharacter[target[j]]
                val jSwap = maxSourceLetterMatchIndex
                val deleteDistance = table[i - 1][j] + 1
                val insertDistance = table[i][j - 1] + 1
                var matchDistance = table[i - 1][j - 1]
                if (!compare(source[i].toString(), target[j].toString())) {
                    matchDistance += 1
                } else {
                    maxSourceLetterMatchIndex = j
                }
                val swapDistance: Int
                if (candidateSwapIndex != null && jSwap != -1) {
                    val preSwapCost: Int
                    if (candidateSwapIndex == 0 && jSwap == 0) {
                        preSwapCost = 0
                    } else {
                        preSwapCost = table[Math.max(0, candidateSwapIndex - 1)][Math.max(0, jSwap - 1)]
                    }
                    swapDistance = (preSwapCost + (i - candidateSwapIndex - 1)
                            + (j - jSwap - 1) + 1)
                } else {
                    swapDistance = Integer.MAX_VALUE
                }
                table[i][j] = Math.min(Math.min(Math
                        .min(deleteDistance, insertDistance), matchDistance), swapDistance)
            }
            sourceIndexByCharacter[source[i]] = i
        }
        return table[source.length - 1][target.length - 1]
    }

    private fun soc_dist(right: String, user: String): Int {
        //int[][] d = new int[right.length()+1][user.length() +1];
        /*char[] s1 = right.toCharArray();
        char[] s2 = user.toCharArray();
        int[] prev = new int[ s2.length + 1 ];

        for( int j = 0; j < s2.length + 1; j++ ) {
            prev[ j ] = j;
        }

        for( int i = 1; i < s1.length + 1; i++ ) {

            // calculate current line of distance matrix
            int[] curr = new int[ s2.length + 1 ];
            curr[0] = i;

            for( int j = 1; j < s2.length + 1; j++ ) {
                int d1 = prev[ j ] + 1;
                int d2 = curr[ j - 1 ] + 1;
                int d3 = prev[ j - 1 ];
                if ( s1[ i - 1 ] != s2[ j - 1 ] ) {
                    d3 += 1;
                }
                curr[ j ] = Math.min( Math.min( d1, d2 ), d3 );
            }

            // define current line of distance matrix as previous
            prev = curr;
        }
        return prev[ s2.length ];*/


        var dist = 0
        var found: Boolean
        for (i in 0 until right.length) {
            found = false
            for (j in 0 until user.length) {
                if (compare(right[i].toString(), user[j].toString())) {
                    //String s = right.charAt()
                    Log.d("SOC_first", "found$i $j")
                    //right = right.replace(Character.toString(right.charAt(i)),"");
                    //user = user.replace(Character.toString(user.charAt(j)),"");
                    found = true
                    break
                }
            }
            if (!found) {
                dist++
            }
        }
        /* if (right.length() == 1 && user.length() == 1 && right.charAt(0)!=user.charAt(0))
            return dist;

        //if (dist == )
        for (int i = 0; i < user.length(); i++){
            found = false;
            for (int j = 0; j < right.length(); j++){
                if (right.charAt(j) == user.charAt(i)) {
                    Log.d("SOC_second", "found"+i+" "+j);
                    found = true;
                    break;
                }
            }
            if (!found) {
                dist++;
            }
        }*/
        return dist
    }

    private fun twog(right: String, user: String): Int {
        val half = right.length / 2
        return soc_dist(right.substring(0, half), user.substring(0, half)) + soc_dist(right.substring(half), user.substring(half))
    }
}
