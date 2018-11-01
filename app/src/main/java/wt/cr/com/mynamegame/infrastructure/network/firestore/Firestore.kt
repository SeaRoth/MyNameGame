package wt.cr.com.mynamegame.infrastructure.network.firestore

import android.content.SharedPreferences
import com.google.firebase.firestore.FirebaseFirestore
import timber.log.Timber
import wt.cr.com.mynamegame.domain.model.Player
import wt.cr.com.mynamegame.infrastructure.di.WTServiceLocator

class Firestore {

    companion object {
        const val DEFAULT_DOC_ID = "-1"
        const val DOC_ID_KEY = "doc_id_key"

        private fun createUser(player: Player) : HashMap<String, Any> {
            val user = HashMap<String, Any>()
            user["name"] = player.name
            user["location"] = player.location
            user["score"] = player.highScore
            user["twoCents"] = player.twoCents
            user["email"] = player.email
            return user
        }

        private fun emptyCallback(){
            Timber.d("sup beetches")
        }

        fun addExistingPlayer(player: Player, collection: String, document: String, callback: () -> Unit?){
            WTServiceLocator.resolve(FirebaseFirestore::class.java)
                    .collection(collection)
                    .document(document)
                    .update(createUser(player))
                    .addOnSuccessListener {
                        callback()
                    }
                    .addOnFailureListener {
                        callback()
                    }
        }

        fun addNewPlayer(player: Player, collection: String, callback: () -> Unit?){
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

        fun populateExampleUsers(){
            val exampleUsersNormal:MutableList<Player> = mutableListOf<Player>(
                    Player("Don Perico","test1@g.com", "The Hut", 5, "i need a dolla"),
                    Player("Nanny Smith","test2@g.com", "1337 pwned way", 5, "/**777db.update()"),
                    Player("Orbit Chiquita Banana","test3@g.com", "Zamsterdam", 4, "China #1")
            )

            val exampleUsersMatt = mutableListOf<Player>(
                    Player("Manny Morales","test14@g.com", "Zamsterdam", 20, "Easy"),
                    Player("Red Dodgers","test15@g.com", "Louisiana", 18, "Carne asuh doo"),
                    Player("Pete Schmitt","test16@g.com", "LA", 22, "Energy")
            )

            val exampleUsersCustom = mutableListOf<Player>(
                    Player("Sea Rothert", "coryr32@gmail.com", "internet", 99, "hey hey"),
                    Player("Charles Brown", "test17@g.com", "oneplus", 3, "809am"),
                    Player("Mercury Baboon","test18@g.com", "4th rock from the sun", 4, "100%")
            )

            exampleUsersCustom.forEach {
                addNewPlayer(it, "custom", this::emptyCallback)
            }

            exampleUsersNormal.forEach {
                addNewPlayer(it, "normal", this::emptyCallback)
            }

            exampleUsersMatt.forEach {
                addNewPlayer(it, "matt", this::emptyCallback)
            }
        }
    }//companion
}//Firestore