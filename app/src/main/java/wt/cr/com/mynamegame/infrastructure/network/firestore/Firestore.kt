package wt.cr.com.mynamegame.infrastructure.network.firestore

import android.util.Log
import com.airbnb.lottie.parser.IntegerParser
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.EventListener
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import wt.cr.com.mynamegame.domain.model.MyModel
import wt.cr.com.mynamegame.infrastructure.di.WTServiceLocator
import com.google.firebase.firestore.QuerySnapshot
import wt.cr.com.mynamegame.infrastructure.ui.home.StatViewModel

class Firestore {

    companion object {

        private fun createUser(player: MyModel.Player) : HashMap<String, Any> {
            val user = HashMap<String, Any>()
            user["name"] = player.name
            user["score"] = player.highScore
            user["twoCents"] = player.twoCents
            return user
        }

        fun addExampleUsers(){
            val player = MyModel.Player("Curry", "Amsterdam, NVM", 12, "I plead the fifth")
            val user = createUser(player)
            addAPlayer(player)
            val player2 = MyModel.Player("Norman", "BRUNO MARS",15, "I plead the fifth")
            val user2 = createUser(player2)
            addAPlayer(player2)
        }

        private fun addAPlayer(player: MyModel.Player){
            WTServiceLocator.resolve(FirebaseFirestore::class.java)
                    .collection("users")
                    .add(player)
                    .addOnSuccessListener{

                        Log.d("TAG", "Added")
                    }
                    .addOnFailureListener{
                        Log.d("TAG", "WE FAILED IT BOIS")
                    }
        }

    }
}