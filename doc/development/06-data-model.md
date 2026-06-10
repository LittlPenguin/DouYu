# Data Model

## Android Model Target

Android models are Java POJOs. Kotlin data classes are not used in Android
production or test source.

## Backend Model Target

Backend entity structure remains Java/Spring based. Runtime demo content must
not be restored; domain tables remain part of the real data model.

## Empty Data Meaning

An empty posts/products/messages/assets result is normal after seed
removal. Empty does not mean the client should create sample rows.

## Seed/Demo Removal

The following runtime data categories are removed:

- Demo posts.
- Demo products and SKUs.
- Demo topics.
- Demo stickers.
- Demo/system content user used for filler.
- Static seed image URLs.

## Test Data

Tests must create explicit fixtures. Fixture names may be test-specific, but
production startup code must not create them.
