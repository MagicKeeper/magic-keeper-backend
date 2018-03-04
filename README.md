# MagicKeeper MicroService Backend

This is supposed to become the MagicKeeper API. It will feature a Lagom microservice architecture with a Play2 API Gateway.


# Services 

This list is clearly not supposed to be final. I have to think a bit more about each service before being able to claim it is final.

Important: we have to remember that the microservices' APIs are at a lower level and not publicly accessible, therefore they don't need to provide comprehensive results.

- [x] `magickeeper-cardlists`: card list management (a card list is a list of cards, detached from its context, with just an ID and a map of cards). 
It is not linked to anyone and has no permissions. This service should absolutely not be acceded from the outside world directly because of this lack of permissions control.
The permissions check should be done at the gateway level (or from the querying service).
- [ ] `magickeeper-decks`: decks management (a deck is composed of multiple cardlists, a name, etc)
- [ ] `magickeeper-collections`: collection management (basically a cardlist)
- [ ] `magickeeper-wishlists`: wishlist management (basically a cardlist again)
- [ ] `magickeeper-friends`
- [ ] `magickeeper-blocklists`
- [ ] `magickeeper-trades`
- [ ] `magickeeper-profiles`
- [ ] `magickeeper-cards`: query some cards or lists of cards
- [ ] `magickeeper-auth`: authentication management