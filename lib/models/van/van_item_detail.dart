import 'package:json_annotation/json_annotation.dart';

part 'van_item_detail.g.dart';

@JsonSerializable()
class VanItemDetails {
  int ItemID;
  int UnitID;
  String ItemName;
  String ItemTypeName;
  String SalesPrice;
  String QtyOnHand;
  String UnitName;

  VanItemDetails(
      {this.ItemID,
      this.UnitID,
      this.ItemName,
      this.ItemTypeName,
      this.SalesPrice,
      this.QtyOnHand,
      this.UnitName});

  // VanItemDetails.fromJson(Map<String, dynamic> map) => $_VanItemDetailsFromJson(map);

  factory VanItemDetails.fromJson(Map<String, dynamic> map) =>
      _$VanItemDetailsFromJson(map);

  Map<String, dynamic> toJson() => _$VanItemDetailsToJson(this);
}

var dummyJson = {
  "ItemID": 2235,
  "UnitID": 30,
  "ItemName": "Cooking",
  "ItemTypeName": "Finished Product",
  "SalesPrice": "38.0",
  "QtyOnHand": "9.00",
  "UnitName": "BDLS",
};
