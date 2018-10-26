package wt.cr.com.mynamegame.infrastructure.network.firestore

import android.content.SharedPreferences
import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import timber.log.Timber
import wt.cr.com.mynamegame.domain.model.MyModel
import wt.cr.com.mynamegame.infrastructure.di.WTServiceLocator

class Firestore {

    companion object {
        const val DOC_ID_KEY = "doc_id_key"

        private fun createUser(player: MyModel.Player) : HashMap<String, Any> {
            val user = HashMap<String, Any>()
            user["name"] = player.name
            user["score"] = player.highScore
            user["twoCents"] = player.twoCents
            return user
        }

        fun addExampleUsers(){
            val player = MyModel.Player("Curry", "Zamsterdam", 2, "MyNameGame can be huge")
            val user = createUser(player)
            //addAPlayer(player, "normal", cb())
            val player2 = MyModel.Player("Norman", "BRUNO MARS",1, "The Fighter and the kid")
            val user2 = createUser(player2)
            //addAPlayer(player2, "normal", cb())

            val player3 = MyModel.Player("Johnny Walker", "123 12th street",3, "Read, study, learn")
            val user4 = createUser(player2)
            //addAPlayer(player2, "normal", cb())
        }


        fun addAPlayer(player: MyModel.Player, collection: String, callback: () -> Unit?){
            WTServiceLocator.resolve(FirebaseFirestore::class.java)
                    .collection(collection)
                    .add(player)
                    .addOnSuccessListener{
                        Timber.d("Just added: ${it.id}")
                        WTServiceLocator.resolve(SharedPreferences::class.java).edit().putString(DOC_ID_KEY, it.id).apply()
                        callback()
                    }
                    .addOnFailureListener{
                        Timber.d("ERROR ADDING")
                        WTServiceLocator.resolve(SharedPreferences::class.java).edit().putString(DOC_ID_KEY, "-1").apply()
                    }
        }

        fun removePlayer(collection: String, docId: String, callback: () -> Unit?){
            WTServiceLocator.resolve(FirebaseFirestore::class.java)
                    .collection(collection)
                    .document(docId)
                    .delete()
                    .addOnSuccessListener {
                        WTServiceLocator.resolve(SharedPreferences::class.java).edit().putString(DOC_ID_KEY, "-1").apply()
                        callback()
                    }
                    .addOnFailureListener {
                        WTServiceLocator.resolve(SharedPreferences::class.java).edit().putString(DOC_ID_KEY, "-1").apply()
                        callback()
                    }
        }
    }
}