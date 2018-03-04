# `cardlist-api`

This service provides a way to read and update **cardlists**.

A **cardlist** is just a map of cards:

    {
        card_id : [
            number_of_cards,
            number_of_foil_cards
        ], ...
    }
    
The card id comes from [MTGJson](https://mtgjson.com). There are **always** two elements in the list.

## Security considerations

The cardlists **don't check permissions** as they don't store them. Therefore, the microservice **SHOULD NOT** be 
directly accessible from the outside.

It's the responsibility of the service or gateway calling this service to check whether or not the user has the right to
read or update a given cardlist.

## Exposed endpoints

- `GET /list/:id`: returns the cardlist associated with this id
  - If the id is not assigned, an empty cardlist will be returned
  - The cardlist corresponds to the format described before
- `POST /list/:id`: updates the cardlist associated with this id
  - The body of the request **MUST** be a JSON object respecting the following format :
    
        {
            card_id : [
                number_of_cards_difference,
                number_of_foil_cards_difference
            ]
        }
        
    For example, the following body
    
        {
            a : [1, 2]
            b : [-1, 0]
            c : [-2, 2]   
        }
        
    adds 1 card a, 2 foil cards a, 2 foil cards c, and removes 1 card b and 2 cards c
  - If the difference is such that the amount of the card becomes negative, a 500 error will be returned and no change
  will be applied. 