# Push to GitHub

## Monorepo (case study / full submission)

**https://github.com/siddharth8763/nimbus-freight**

Contains `backend/`, `frontend/`, `starter_kit/`, root `SOLUTION_DESIGN.md`, `DOCUMENTATION.md`, and start scripts.

From a machine that has the umbrella repo cloned:

```powershell
git add -A
git commit -m "your message"
git push origin main
```

To refresh the monorepo from this split layout (no nested `.git` in the copy), sync files into a clone of `nimbus-freight`, then commit and push there. Exclude `backend/.git`, `backend/target`, `frontend/.git`, and `frontend/node_modules`.

## Split repositories

| Folder | Remote |
| --- | --- |
| `backend/` | https://github.com/siddharth8763/nimbus-freight-backend.git |
| `frontend/` | https://github.com/siddharth8763/nimbus-freight-frontend.git |

```powershell
git -C backend push -u origin main
git -C frontend push -u origin main
```

Keep all three in sync when you change backend or frontend: push the split repo, then update the monorepo copy.
