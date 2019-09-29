package jp.techacademy.kazuyoshi.takahashi.qa_app

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import jp.techacademy.kazuyoshi.takahashi.qa_app.R.id.imgBookmark

class QuestionDetailListAdapter(context: Context, private val mQustion: Question) : BaseAdapter() {
    companion object {
        private val TYPE_QUESTION = 0
        private val TYPE_ANSWER = 1
    }

    private var mLayoutInflater: LayoutInflater? = null
    private var bookmarkFlg = false

    init {
        mLayoutInflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
    }

    override fun getCount(): Int {
        return 1 + mQustion.answers.size
    }

    override fun getItemViewType(position: Int): Int {
        return if (position == 0) {
            TYPE_QUESTION
        } else {
            TYPE_ANSWER
        }
    }

    override fun getViewTypeCount(): Int {
        return 2
    }

    override fun getItem(position: Int): Any {
        return mQustion
    }

    override fun getItemId(position: Int): Long {
        return 0
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var convertView = convertView

        if (getItemViewType(position) == TYPE_QUESTION) {
            if (convertView == null) {
                convertView = mLayoutInflater!!.inflate(R.layout.list_question_detail, parent, false)!!
            }
            val body = mQustion.body
            val name = mQustion.name

            val bodyTextView = convertView.findViewById<View>(R.id.bodyTextView) as TextView
            bodyTextView.text = body

            val nameTextView = convertView.findViewById<View>(R.id.nameTextView) as TextView
            nameTextView.text = name

            val bytes = mQustion.imageBytes
            if (bytes.isNotEmpty()) {
                val image = BitmapFactory.decodeByteArray(bytes, 0, bytes.size).copy(Bitmap.Config.ARGB_8888, true)
                val imageView = convertView.findViewById<View>(R.id.imageView) as ImageView
                imageView.setImageBitmap(image)
            }

            //ここでBookmark関連のonCreateでの処理をする。
            onCreateBookmark(mQustion, convertView)


        } else {
            if (convertView == null) {
                convertView = mLayoutInflater!!.inflate(R.layout.list_answer, parent, false)!!
            }

            val answer = mQustion.answers[position - 1]
            val body = answer.body
            val name = answer.name

            val bodyTextView = convertView.findViewById<View>(R.id.bodyTextView) as TextView
            bodyTextView.text = body

            val nameTextView = convertView.findViewById<View>(R.id.nameTextView) as TextView
            nameTextView.text = name
        }

        return convertView
    }

    fun onCreateBookmark(question: Question, convertView: View?) {
        //ユーザーの取得
        val user = FirebaseAuth.getInstance().currentUser

        val imgBookmark = convertView!!.findViewById<View>(R.id.imgBookmark) as ImageButton
        if (user == null){
            imgBookmark.visibility = View.INVISIBLE
        }

        if (user != null) {

            val dataBaseReference = FirebaseDatabase.getInstance().reference
            val bookmarkQuestionRef =
                    dataBaseReference.
                            child(BookmarkPATH).
                            child(user!!.uid).
                            child(question.questionUid)
            //取得
            val imgBookmark = convertView!!.findViewById<View>(R.id.imgBookmark) as ImageButton
            //bookmarkQuestionRefのデータは以下のように取得する
            bookmarkQuestionRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val data = snapshot.value as Map<*, *>?
                    if (data != null) {
                        imgBookmark.setImageResource(R.drawable.store_heart)
                        bookmarkFlg = true
                    } else {
                        imgBookmark.setImageResource(R.drawable.store_heart_white)
                        bookmarkFlg = false
                    }
                }
                override fun onCancelled(snapshot: DatabaseError) {
                }
            })
            //お気に入りボタンのクリックリスナー設定
            imgBookmark.setOnClickListener {
                ClickBookmark(question, imgBookmark)
            }
        }
    }


    fun ClickBookmark(question: Question, imgBookmark: ImageButton) {
        val user = FirebaseAuth.getInstance().currentUser
        val dataBaseReference = FirebaseDatabase.getInstance().reference
        val userbookmarkRef = dataBaseReference.child(BookmarkPATH).child(user!!.uid).child(question.questionUid)

        bookmarkFlg = !bookmarkFlg
        if (bookmarkFlg) {

            val data = HashMap<String, String>()
            val genre = question.genre.toString()
            //データにgenre（ジャンル）を設定する必要があるので、作成。

            data["genre"] = genre
            userbookmarkRef.setValue(data)//これで、questionUidごとGenreも削除になる。
            imgBookmark.setImageResource(R.drawable.store_heart)
        }
        else {
            userbookmarkRef.removeValue()
            imgBookmark.setImageResource(R.drawable.store_heart_white)
        }
    }
}