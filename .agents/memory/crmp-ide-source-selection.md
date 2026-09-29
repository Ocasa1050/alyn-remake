---
name: CRMP IDE source selection
description: Durable compatibility note for choosing IDE files from the CRMP release cache.
---

The CRMP release cache contains both `data/default.ide` and `data/vehicles.ide` plus SAMP-named copies. The SAMP vehicle file is not interchangeable with the base GTA data copy and has malformed vehicle rows in the published cache.

**Why:** Forcing the SAMP vehicle IDE into `CFileLoader::LoadLevel` can leave `CVehicleModelInfo` data invalid and lead to a null dereference in `LoadEnvironmentMaps`, even when the cache is complete.

**How to apply:** Prefer the original `data` IDE files when they exist; use the SAMP copies only as an automatic fallback when the original file is genuinely absent.