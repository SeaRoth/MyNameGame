package wt.cr.com.mynamegame.infrastructure.network.firestore

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import wt.cr.com.mynamegame.domain.model.MyModel
import wt.cr.com.mynamegame.infrastructure.di.WTServiceLocator

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
            val player = MyModel.Player("Curry", "Amsterdam, NVM", 2, "Charlie Brown")
            val user = createUser(player)
            addAPlayer(player)
            val player2 = MyModel.Player("Norman", "BRUNO MARS",1, "The Fighter and the kid")
            val user2 = createUser(player2)
            addAPlayer(player2)

            val player3 = MyModel.Player("Johnny Walker", "123 12th street",3, "Morning Rituals")
            val user4 = createUser(player2)
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