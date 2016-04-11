//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//    http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
//

import UIKit
import MarkupKit
import HTTPRPC

class MainViewController: UITableViewController {
    var listNotesTask: NSURLSessionDataTask!

    var notes: [[String: AnyObject]] = []

    static let NoteCellIdentifier = "noteCell"

    override func viewDidLoad() {
        super.viewDidLoad()

        title = NSBundle.mainBundle().localizedStringForKey("notes", value: nil, table: nil)

        navigationItem.rightBarButtonItem = UIBarButtonItem(barButtonSystemItem: UIBarButtonSystemItem.Add,
            target: self, action: #selector(MainViewController.add))
    }

    override func viewWillAppear(animated: Bool) {
        super.viewWillAppear(animated)

        listNotesTask = AppDelegate.serviceProxy.invoke("listNotes") {(result, error) in
            if (error == nil) {
                self.notes = result as! [[String: AnyObject]]

                self.tableView.reloadData()
            } else {
                let alertController = UIAlertController(title: error!.domain,
                    message: error!.localizedDescription,
                    preferredStyle: .Alert)

                alertController.addAction(UIAlertAction(title: NSBundle.mainBundle().localizedStringForKey("ok", value: nil, table: nil),
                    style: .Default, handler: nil))

                self.presentViewController(alertController, animated: true, completion: nil)
            }

            self.listNotesTask = nil
        }
    }

    override func viewWillDisappear(animated: Bool) {
        super.viewWillDisappear(animated)

        if (listNotesTask != nil) {
            listNotesTask.cancel()
        }
    }

    override func numberOfSectionsInTableView(tableView: UITableView) -> Int {
        return 1
    }

    override func tableView(tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        return notes.count
    }

    override func tableView(tableView: UITableView, cellForRowAtIndexPath indexPath: NSIndexPath) -> UITableViewCell {
        let message = notes[indexPath.row]["message"] as? String
        let date = NSDate(timeIntervalSince1970: notes[indexPath.row]["date"] as! Double)

        var cell: UITableViewCell! = tableView.dequeueReusableCellWithIdentifier(MainViewController.NoteCellIdentifier)

        if (cell == nil) {
            cell = UITableViewCell(style: .Subtitle, reuseIdentifier: MainViewController.NoteCellIdentifier)
        }

        cell.textLabel!.text = message
        cell.detailTextLabel!.text = NSDateFormatter.localizedStringFromDate(date, dateStyle: .ShortStyle, timeStyle: .MediumStyle)

        return cell
    }

    func add() {
        presentViewController(UINavigationController(rootViewController:AddNoteViewController()), animated: true, completion: nil)
    }
}