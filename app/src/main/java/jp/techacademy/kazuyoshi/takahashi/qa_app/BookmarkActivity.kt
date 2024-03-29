package jp.techacademy.kazuyoshi.takahashi.qa_app

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Base64
import android.widget.ListView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class BookmarkActivity : AppCompatActivity() {

    private lateinit var mBookmarkRef: DatabaseReference
    private lateinit var mQuestionArrayList: ArrayList<Question>
    private lateinit var mListView: ListView
    private lateinit var mAdapter: QuestionsListAdapter
    private val user = FirebaseAuth.getInstance().currentUser
    private val databaseReference = FirebaseDatabase.getInstance().reference
    private val answerArrayList = ArrayList<Answer>()

    private val mEventListener = object : ChildEventListener {
        override fun onChildAdded(snapshot: DataSnapshot, p1: String?) {
            val map = snapshot.value as Map<String, String>
            val genre = map["genre"] ?: ""
            val ref = databaseReference.child(ContentsPATH).child(genre).child(snapshot.key as String)
            ref.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(p0: DataSnapshot) {
                    val data = p0.value as Map<String, String>?
                    if (data != null) {
                        val title = data["title"] ?: ""
                        val body = data["body"] ?: ""
                        val name = data["name"] ?: ""
                        val uid = data["uid"] ?: ""
                        val imageString = data["image"] ?: ""
                        val bytes = if (imageString.isNotEmpty()) {
                            Base64.decode(imageString, Base64.DEFAULT)
                        } else {
                            byteArrayOf()
                        }

                        val question =
                                Question(title, body, name, uid, snapshot.key ?: "", genre.toInt(), bytes, answerArrayList)
                        mQuestionArrayList.add(question)
                        mAdapter.notifyDataSetChanged()

                    }
                }

                override fun onCancelled(p0: DatabaseError) {
                }
            })

            // 質問のリストをクリアしてから再度Adapterにセットし、AdapterをListViewにセットし直す
            mQuestionArrayList.clear()
            mAdapter.setQuestionArrayList(mQuestionArrayList)
            mListView.adapter = mAdapter
        }

        override fun onChildChanged(p0: DataSnapshot, p1: String?) {
        }

        override fun onChildRemoved(snapshot: DataSnapshot) {
        }

        override fun onChildMoved(p0: DataSnapshot, p1: String?) {
        }

        override fun onCancelled(p0: DatabaseError) {
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bookmark)

        title = "お気に入り"

    }

    override fun onResume() {
        super.onResume()
        // ListViewの準備
        mListView = findViewById(R.id.bookmarkListView)
        mAdapter = QuestionsListAdapter(this)
        mQuestionArrayList = ArrayList()
        mAdapter.notifyDataSetChanged()

        mBookmarkRef = databaseReference.child(BookmarkPATH).child(user!!.uid)
        mBookmarkRef.addChildEventListener(mEventListener)

        mListView.setOnItemClickListener { parent, view, position, id ->
            val intent = Intent(this,QuestionDetailActivity::class.java)
            intent.putExtra("question",mQuestionArrayList[position])
            startActivity(intent)
        }
    }
}