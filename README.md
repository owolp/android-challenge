# Syft coding challenge

## Tasks

There's a series of tasks to complete for the coding challenge.
Make sure you **add unit tests** for code that you write!
 
In no particular order, the tasks are:

#### 1. Add pagination
The source data comes from [here](https://jsonplaceholder.typicode.com/posts) so you can tweak the URL to read `?_page=2&_limit=20` (for example) to make the data paginated.

Bonus: leave some comments in the code around this specific way of paginating, and if there are any issues you can see by fetching "page 1", "page 2", and limiting.

#### 2. Animate new items coming in
New items being added to the recyclerview should animate in when a new page is loaded.

#### 3. Delete a post from the list
Add something to a list item that allows you to delete that item. The API will accept the `DELETE` verb, but subsequent fetches will return the item again; this will be expected when reviewing the test.

Bonus: animate the list item removal so that the other items move in to its place gracefully.

#### 4. Fix broken tests
There are some unit tests that are broken, fix them.

#### 5. (Bonus) Delete a post from the post details screen
The behaviour of this action is up to you. A couple of ideas could be:

1. Soft delete, and update the screen to tell the user the post way deleted. This should also be reflected in the list screen.
2. Delete the post from the database, kick the user off the screen, and remove the item from the recyclerview on the list screen, with a message saying it has been deleted.

## What we are looking for

Please don't spend more than 3 hours on the test, we understand that your time is important.

- Clear, easy to read, self-documenting code.
- Clear, concise, and easy to read unit tests.
- Consistency with existing code: Architecturally, semantically, and idiomatically, unless you believe there's a compelling reason to deviate. If so, please document it.
- Commit history: we should be able to follow how you approached the problem, what the iterations were, and how roughly long it took.
- Task completion.

Please provide any additional information that you want to communicate back to the reviewer here:

